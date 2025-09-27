package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.block.AbstractInventoryHopperBlock;
import com.tom.storagemod.inventory.NetworkInventory;
import com.tom.storagemod.platform.PlatformBlockEntity;
import com.tom.storagemod.util.IValidInfo;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public abstract class AbstractInventoryHopperBlockEntity extends PlatformBlockEntity implements TickableServer, IValidInfo {
	protected NetworkInventory topCache = new NetworkInventory();
	protected NetworkInventory bottomCache = new NetworkInventory();

	public AbstractInventoryHopperBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide()) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(AbstractInventoryHopperBlock.FACING);
			bottomCache.onLoad(level, worldPosition.relative(facing), facing.getOpposite(), this);
			topCache.onLoad(level, worldPosition.relative(facing.getOpposite()), facing, this);
		}
	}

	public boolean isEnabled() {
		return this.getBlockState().getValue(AbstractInventoryHopperBlock.ENABLED);
	}
}
