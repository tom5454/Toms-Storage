package com.tom.storagemod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TermRangeCalc {

	public static int calcBeaconLevel(Level world, int x, int y, int z) {
		int i = 0;

		BlockEntity ent = world.getBlockEntity(new BlockPos(x, y, z));
		if(ent instanceof BeaconBlockEntity b) {
			if(b.getBeamSections().isEmpty())return 0;

			for(int j = 1; j <= 4; i = j++) {
				int k = y - j;
				if (k < world.getMinBuildHeight()) {
					break;
				}

				boolean flag = true;

				for(int l = x - j; l <= x + j && flag; ++l) {
					for(int i1 = z - j; i1 <= z + j; ++i1) {
						if (!world.getBlockState(new BlockPos(l, k, i1)).is(BlockTags.BEACON_BASE_BLOCKS)) {
							flag = false;
							break;
						}
					}
				}

				if (!flag) {
					break;
				}
			}
		}
		return i;
	}
}
