package com.tom.storagemod.tile;

import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryProxy;

public class TileEntityInventoryProxy extends TileEntityPainted implements ITickableTileEntity {
	private LazyOptional<IItemHandler> invHandler;
	private LazyOptional<IItemHandler> pointedAt;
	public TileEntityInventoryProxy() {
		super(StorageMod.invProxyTile);
	}
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryProxy.FACING);
			if(side != facing) {
				if (this.invHandler == null)
					this.invHandler = LazyOptional.of(InvHandler::new);
				return this.invHandler.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	private class InvHandler implements IItemHandler, IProxy {

		private boolean calling;
		public <R> R call(Function<IItemHandler, R> func, R def) {
			if(calling)return def;
			calling = true;
			if(pointedAt != null) {
				R r = func.apply(pointedAt.orElse(EmptyHandler.INSTANCE));
				calling = false;
				return r;
			}
			calling = false;
			return def;
		}

		@Override
		public int getSlots() {
			return call(IItemHandler::getSlots, 0);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return call(i -> i.getStackInSlot(slot), ItemStack.EMPTY);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return call(i -> i.insertItem(slot, stack, simulate), stack);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return call(i -> i.extractItem(slot, amount, simulate), ItemStack.EMPTY);
		}

		@Override
		public int getSlotLimit(int slot) {
			return call(i -> i.getSlotLimit(slot), 0);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return call(i -> i.isItemValid(slot, stack), false);
		}

		@Override
		public IItemHandler get() {
			if(pointedAt != null) {
				return pointedAt.orElse(null);
			}
			return null;
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (invHandler != null)
			invHandler.invalidate();
	}
	@Override
	public void tick() {
		if(!world.isRemote && world.getGameTime() % 20 == 18) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryProxy.FACING);
			if(pointedAt == null || !pointedAt.isPresent()) {
				TileEntity te = world.getTileEntity(pos.offset(facing));
				if(te != null && !(te instanceof TileEntityInventoryProxy)) {
					pointedAt = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				}
			}
		}
	}
}
