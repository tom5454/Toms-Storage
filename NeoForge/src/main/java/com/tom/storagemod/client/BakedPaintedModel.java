package com.tom.storagemod.client;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import com.tom.storagemod.block.entity.PaintedBlockEntity;

public class BakedPaintedModel implements DynamicBlockStateModel {
	private Block blockFor;
	private BlockStateModel parent;
	public BakedPaintedModel(Block blockFor, BlockStateModel parent) {
		this.blockFor = blockFor;
		this.parent = parent;
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return parent.particleIcon();
	}

	@Override
	public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return parent.particleIcon(level, pos, state);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
			List<BlockModelPart> parts) {
		BlockStateModel model = null;
		Supplier<BlockState> blockstateSupp = level.getModelData(pos).get(PaintedBlockEntity.FACADE_STATE);
		BlockState blockstate = null;
		if(blockstateSupp != null)blockstate = blockstateSupp.get();
		if (blockstate == null || blockstate == Blocks.AIR.defaultBlockState()) {
			blockstate = state;
			model = parent;
		}

		if(model == null)
			model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockstate);

		if (model instanceof BakedPaintedModel)return;

		model.collectParts(level, pos, blockstate, random, parts);
	}
}
