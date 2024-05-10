package com.tom.storagemod.tile;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryProxyBlockEntity extends PaintedBlockEntity implements TickableServer {
	public IItemHandler invHandler = new InvHandler();
	private BlockCapabilityCache<IItemHandler, Direction> pointedAt;

	public InventoryProxyBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invProxyTile.get(), pos, state);
	}

	private boolean calling;
	public <R> R call(Function<IItemHandler, R> func, R def) {
		if(calling)return def;
		calling = true;
		if(pointedAt != null) {
			IItemHandler ih = pointedAt.getCapability();
			if(ih != null) {
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
			return pointedAt.getCapability();
		}
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 18) {
			this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			pointedAt = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) level, worldPosition.relative(facing), facing.getOpposite());
		}
	}
}
