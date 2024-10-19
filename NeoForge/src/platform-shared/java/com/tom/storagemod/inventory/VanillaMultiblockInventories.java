package com.tom.storagemod.inventory;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class VanillaMultiblockInventories {

	public static void checkChest(Level level, BlockPos p, BlockState state, Consumer<BlockPos> extra) {
		if(state.getBlock() instanceof ChestBlock) {
			ChestType type = state.getValue(ChestBlock.TYPE);
			if (type != ChestType.SINGLE) {
				BlockPos opos = p.relative(ChestBlock.getConnectedDirection(state));
				BlockState ostate =level.getBlockState(opos);
				if (state.getBlock() == ostate.getBlock()) {
					ChestType otype = ostate.getValue(ChestBlock.TYPE);
					if (otype != ChestType.SINGLE && type != otype && state.getValue(ChestBlock.FACING) == ostate.getValue(ChestBlock.FACING)) {
						extra.accept(opos);
					}
				}
			}
		}
	}
}
