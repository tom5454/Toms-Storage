package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.IInventoryCable;

public class TileEntityInventoryCableConnector extends TileEntity implements ITickableTileEntity {
	private long foundTime;
	private TileEntityInventoryCableConnector otherSide;
	private LazyOptional<IItemHandler> invHandler;
	private LazyOptional<IItemHandler> pointedAt;
	public TileEntityInventoryCableConnector() {
		super(StorageMod.invCableConnectorTile);
	}

	@Override
	public void tick() {
		if(!world.isRemote && world.getGameTime() % 20 == 19) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			if(world.getGameTime() != foundTime) {
				DyeColor color = state.get(BlockInventoryCableConnector.COLOR);
				Stack<BlockPos> toCheck = new Stack<>();
				Set<BlockPos> checkedBlocks = new HashSet<>();
				checkedBlocks.add(pos);
				toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, pos));
				otherSide = null;
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(world.isBlockLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.invCableConnector) {
								if(state.get(BlockInventoryCableConnector.COLOR) == color) {
									TileEntity te = world.getTileEntity(cp);
									if(te instanceof TileEntityInventoryCableConnector) {
										otherSide = (TileEntityInventoryCableConnector) te;
										otherSide.foundTime = world.getGameTime();
										otherSide.otherSide = this;
									}
									break;
								}
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
							}
						}
						if(checkedBlocks.size() > Config.invConnectorMax)break;
					}
				}
			}
			if(pointedAt == null || !pointedAt.isPresent()) {
				TileEntity te = world.getTileEntity(pos.offset(facing));
				if(te != null) {
					pointedAt = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				}
			}
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (this.invHandler == null)
				this.invHandler = LazyOptional.of(InvHandler::new);
			return this.invHandler.cast();
		}
		return super.getCapability(cap, side);
	}

	private class InvHandler implements IItemHandler, IProxy {

		private boolean calling;
		public <R> R call(Function<IItemHandler, R> func, R def) {
			if(calling)return def;
			calling = true;
			if(otherSide != null && !otherSide.isRemoved() && otherSide.pointedAt != null) {
				R r = func.apply(otherSide.pointedAt.orElse(EmptyHandler.INSTANCE));
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
			if(otherSide != null && !otherSide.isRemoved() && otherSide.pointedAt != null) {
				return otherSide.pointedAt.orElse(null);
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
}
