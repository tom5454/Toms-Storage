package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.AbstractInventoryHopperBlock;
import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventory;
import com.tom.storagemod.inventory.PlatformInventoryAccess.BlockInventoryAccess;
import com.tom.storagemod.inventory.PlatformProxyInventoryAccess;

public class InventoryProxyBlockEntity extends PaintedBlockEntity implements IInventory {
	private BlockInventoryAccess block = new BlockInventoryAccess() {

		@Override
		protected void onInvalid() {
			markCapsInvalid();
		}
	};
	private PlatformProxyInventoryAccess proxy = new PlatformProxyInventoryAccess(block);

	public InventoryProxyBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invProxyBE.get(), pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(AbstractInventoryHopperBlock.FACING);
			block.onLoad(level, worldPosition.relative(facing), facing.getOpposite(), this);
		}
	}

	@Override
	public IInventoryAccess getInventoryAccess() {
		return proxy;
	}
}
