package com.tom.storagemod.client;

import java.util.List;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.block.entity.PaintedBlockEntity;

public class BakedPaintedModel implements BakedModel {
	private BakedModel parent;

	public BakedPaintedModel(BakedModel parent) {
		this.parent = parent;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
			Supplier<RandomSource> randomSupplier, RenderContext context) {
		BlockEntity tile = blockView.getBlockEntity(pos);
		if(tile instanceof PaintedBlockEntity) {
			BakedModel model = null;
			try {
				BlockState blockstate = ((PaintedBlockEntity)tile).getPaintedBlockState();
				if (blockstate == null || blockstate == Blocks.AIR.defaultBlockState()) {
					blockstate = state;
					model = parent;
				}
				if(model == null)
					model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockstate);
				if (!(model instanceof BakedPaintedModel)) {
					model.emitBlockQuads(blockView, state, pos, randomSupplier, context);
					return;
				}
			} catch (Exception e) {
			}
		}

		QuadEmitter emitter = context.getEmitter();

		for(Direction direction : Direction.values()) {
			// Add a new face to the mesh
			emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
			// Set the sprite of the face, must be called after .square()
			// We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
			emitter.spriteBake(getParticleIcon(), MutableQuadView.BAKE_LOCK_UV);
			// Enable texture usage
			emitter.color(0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000);
			// Add the quad to the mesh
			emitter.emit();
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {

	}

	@Override
	public ItemOverrides getOverrides() {
		return null;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState arg0, Direction arg1, RandomSource arg2) {
		return parent.getQuads(arg0, arg1, arg2);
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return parent.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return null;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}
}
