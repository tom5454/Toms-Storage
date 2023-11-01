package com.tom.storagemod.tile;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryProxyBlockEntity extends PaintedBlockEntity implements TickableServer {
	private LazyOptional<IItemHandler> invHandler;
	private LazyOptional<IItemHandler> pointedAt;

	public InventoryProxyBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invProxyTile.get(), pos, state);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.remove && cap == Capabilities.ITEM_HANDLER) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			if(side != facing) {
				if (this.invHandler == null)
					this.invHandler = LazyOptional.of(InvHandler::new);
				return this.invHandler.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	private boolean calling;
	public <R> R call(Function<IItemHandler, R> func, Predicate<IItemHandler> accessCheck, R def) {
		if(calling)return def;
		calling = true;
		if(pointedAt != null) {
			IItemHandler ih = pointedAt.orElse(EmptyHandler.INSTANCE);
			if(accessCheck.test(ih)) {
				R r = func.apply(ih);
				calling = false;
				return r;
			}
		}
		calling = false;
		return def;
	}

	private class InvHandler implements IItemHandler, IProxy {

		@Override
		public int getSlots() {
			return call(IItemHandler::getSlots, i -> true, 0);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return call(i -> i.getStackInSlot(slot), i -> checkFilter(i, slot, null), ItemStack.EMPTY);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return call(i -> i.insertItem(slot, stack, simulate), i -> checkFilter(i, slot, stack), stack);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return call(i -> i.extractItem(slot, amount, simulate), i -> checkFilter(i, slot, null), ItemStack.EMPTY);
		}

		@Override
		public int getSlotLimit(int slot) {
			return call(i -> Math.min(i.getSlotLimit(slot), getMaxCountPerStack()), i -> true, 0);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return call(i -> i.isItemValid(slot, stack), i -> checkFilter(i, slot, stack), false);
		}

		@Override
		public IItemHandler get() {
			if(pointedAt != null) {
				return pointedAt.orElse(null);
			}
			return null;
		}
	}

	private int getMaxCountPerStack() {
		return 64;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (invHandler != null)
			invHandler.invalidate();
	}
	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 18) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			if(pointedAt == null || !pointedAt.isPresent()) {
				BlockEntity te = level.getBlockEntity(worldPosition.relative(facing));
				if(te != null && !(te instanceof InventoryProxyBlockEntity)) {
					pointedAt = te.getCapability(Capabilities.ITEM_HANDLER, facing.getOpposite());
				}
			}
			this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
		}
	}

	private boolean checkFilter(IItemHandler w, int slot, ItemStack stack) {
		return true;
	}
}
