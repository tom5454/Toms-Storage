package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import net.minecraft.block.BlockState;
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
import com.tom.storagemod.tile.TileEntityInventoryConnector.LinkedInv;

public class TileEntityInventoryCableConnector extends TileEntity implements ITickableTileEntity {
	private TileEntityInventoryConnector master;
	private LazyOptional<IItemHandler> invHandler;
	private LazyOptional<IItemHandler> pointedAt;
	private LinkedInv linv;
	public TileEntityInventoryCableConnector() {
		super(StorageMod.invCableConnectorTile);
	}

	@Override
	public void tick() {
		if(!world.isRemote && world.getGameTime() % 20 == 19) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(pos);
			toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, pos));
			if(master != null)master.unLink(linv);
			master = null;
			linv = new LinkedInv();
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				if(!checkedBlocks.contains(cp)) {
					checkedBlocks.add(cp);
					if(world.isBlockLoaded(cp)) {
						state = world.getBlockState(cp);
						if(state.getBlock() == StorageMod.connector) {
							TileEntity te = world.getTileEntity(cp);
							if(te instanceof TileEntityInventoryConnector) {
								master = (TileEntityInventoryConnector) te;
								linv.time = world.getGameTime();
								linv.handler = () -> (pointedAt == null ? LazyOptional.empty() : pointedAt);
								master.addLinked(linv);
							}
							break;
						}
						if(state.getBlock() instanceof IInventoryCable) {
							toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
						}
					}
					if(checkedBlocks.size() > Config.invConnectorMax)break;
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
			if(master != null && !master.isRemoved()) {
				R r = func.apply(master.getInventory().orElse(EmptyHandler.INSTANCE));
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
			if(master != null && !master.isRemoved()) {
				return master.getInventory().orElse(null);
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
