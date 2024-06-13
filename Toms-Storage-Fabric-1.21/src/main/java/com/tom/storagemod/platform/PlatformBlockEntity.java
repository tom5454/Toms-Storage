package com.tom.storagemod.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.util.IValidInfo;
import com.tom.storagemod.util.TickerUtil.TickableServer;
import com.tom.storagemod.util.TickerUtil.TickableServer0;

public class PlatformBlockEntity extends BlockEntity implements IValidInfo, TickableServer0 {
	private boolean loaded = false;

	public PlatformBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);
	}

	@Override
	public boolean isObjectValid() {
		return !isRemoved();
	}

	public void onLoad() {
	}

	@Override
	public void clearRemoved() {
		super.clearRemoved();
		loaded = false;
	}

	@Override
	public final void updateServer0() {
		if(!loaded) {
			onLoad();
			loaded = true;
		}
		if(this instanceof TickableServer t)
			t.updateServer();
	}
}
