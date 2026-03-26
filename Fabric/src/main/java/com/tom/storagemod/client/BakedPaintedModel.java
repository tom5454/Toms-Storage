package com.tom.storagemod.client;

import java.util.List;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialFlags;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.util.RandomSource;

public class BakedPaintedModel implements BlockStateModel {
	private BlockStateModel parent;

	public BakedPaintedModel(BlockStateModel parent) {
		this.parent = parent;
	}

	@Override
	public Baked particleMaterial() {
		return parent.particleMaterial();
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		parent.collectParts(random, output);
	}

	@Override
	public @MaterialFlags int materialFlags() {
		return parent.materialFlags();
	}

	/*@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state,
			RandomSource random, Predicate<@Nullable Direction> cullTest) {
		BlockEntity tile = blockView.getBlockEntity(pos);
		if(tile instanceof PaintedBlockEntity) {
			BlockStateModel model = null;
			try {
				BlockState blockstate = ((PaintedBlockEntity)tile).getPaintedBlockState();
				if (blockstate == null || blockstate == Blocks.AIR.defaultBlockState()) {
					blockstate = state;
					model = parent;
				}
				if(model == null)
					model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockstate);
				if (!(model instanceof BakedPaintedModel)) {
					model.emitQuads(emitter, blockView, pos, blockstate, random, cullTest);
					return;
				}
			} catch (Exception e) {
			}
		}

		for(Direction direction : Direction.values()) {
			// Add a new face to the mesh
			emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
			// Set the sprite of the face, must be called after .square()
			// We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
			emitter.spriteBake(particleIcon(), MutableQuadView.BAKE_LOCK_UV);
			// Enable texture usage
			emitter.color(0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000);
			// Add the quad to the mesh
			emitter.emit();
		}
	}

	@Override
	public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		return parent.particleSprite(blockView, pos, state);
	}

	@Override
	public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
		parent.collectParts(randomSource, list);
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return parent.particleIcon();
	}*/
}
