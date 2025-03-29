package com.tom.storagemod.client;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.block.entity.PaintedBlockEntity;

public class BakedPaintedModel implements BlockStateModel {
	private BlockStateModel parent;

	public BakedPaintedModel(BlockStateModel parent) {
		this.parent = parent;
	}

	@Override
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
	}
}
