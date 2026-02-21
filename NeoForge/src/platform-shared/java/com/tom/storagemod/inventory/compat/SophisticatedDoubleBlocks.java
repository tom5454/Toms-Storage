package com.tom.storagemod.inventory.compat;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.util.IDoubleBlock;

import com.tom.storagemod.api.MultiblockInventoryAPI;

public class SophisticatedDoubleBlocks {
	public static void register() {
		MultiblockInventoryAPI.EVENT.register(SophisticatedDoubleBlocks::checkChest);
	}

	public static void checkChest(Level level, BlockPos p, BlockState state, Consumer<BlockPos> extra) {
		if (state.getBlock() instanceof IDoubleBlock db) {
			db.getOtherPosition(state, p).ifPresent(extra);
		}
	}
}
