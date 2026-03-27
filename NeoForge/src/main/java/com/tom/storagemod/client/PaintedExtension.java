package com.tom.storagemod.client;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

import com.tom.storagemod.block.entity.PaintedBlockEntity;

import it.unimi.dsi.fastutil.ints.IntList;

public class PaintedExtension implements IClientBlockExtensions {

	@Override
	public void collectDynamicTintValues(BlockState state, BlockAndTintGetter level, BlockPos pos, IntList tintValues) {
		Supplier<BlockState> blockstateSupp = level.getModelData(pos).get(PaintedBlockEntity.FACADE_STATE);
		BlockState blockstate = null;
		if(blockstateSupp != null)blockstate = blockstateSupp.get();
		if (blockstate == null || blockstate == Blocks.AIR.defaultBlockState()) {
			return;
		}
		List<BlockTintSource> tintSources = Minecraft.getInstance().getBlockColors().getTintSources(blockstate);
		int tintSourceCount = tintSources.size();
		if (tintSourceCount == 0)return;

		for (int i = 0; i < tintSourceCount; i++) {
			var source = tintSources.get(i);
			int color = source.colorInWorld(blockstate, level, pos);
			tintValues.add(color);
		}
	}
}
