package com.tom.storagemod.client;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.tom.storagemod.block.entity.PaintedBlockEntity;

public class BakedPaintedModel implements IDynamicBakedModel {
	private Block blockFor;
	private BakedModel parent;
	public BakedPaintedModel(Block blockFor, BakedModel parent) {
		this.blockFor = blockFor;
		this.parent = parent;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return parent.getParticleIcon(ModelData.EMPTY);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
			@NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType layer) {
		BakedModel model = null;
		Supplier<BlockState> blockstateSupp = modelData.get(PaintedBlockEntity.FACADE_STATE);
		BlockState blockstate = null;
		if(blockstateSupp != null)blockstate = blockstateSupp.get();
		if (blockstate == null || blockstate == Blocks.AIR.defaultBlockState()) {
			blockstate = state;
			model = parent;
			if(layer != null && layer != RenderType.solid())return Collections.emptyList();
		}

		if(model == null)
			model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockstate);
		return model.getQuads(blockstate, side, rand, ModelData.EMPTY, layer);
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData) {
		return tileData;
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
			@NotNull ModelData data) {
		return ChunkRenderTypeSet.all();
	}
}
