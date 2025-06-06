package com.tom.storagemod.api;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockInventoryAPI {
	public static final EventHandler<MultiblockDetector> EVENT = new EventHandler<>(MultiblockDetector.class, (listeners) -> (level, pos, state, extra) -> {
		for (MultiblockDetector event : listeners) {
			event.detectMultiblocks(level, pos, state, extra);
		}
	});

	@FunctionalInterface
	public static interface MultiblockDetector {
		void detectMultiblocks(Level level, BlockPos pos, BlockState state, Consumer<BlockPos> extra);
	}
}
