package com.tom.storagemod.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import com.mojang.datafixers.util.Pair;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.tile.TileEntityPainted;

public class BakedPaintedModel implements UnbakedModel, BakedModel, FabricBakedModel {
	private static final SpriteIdentifier ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(StorageMod.modid, "block/trim"));
	private static final Identifier FALLBACK_ID = new Identifier("minecraft:block/stone");
	private Sprite sprite;
	private BakedModel fallback;
	public BakedPaintedModel() {
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos,
			Supplier<Random> randomSupplier, RenderContext context) {
		BlockEntity tile = blockView.getBlockEntity(pos);
		if(tile instanceof TileEntityPainted) {
			try {
				BlockState st = ((TileEntityPainted)tile).getPaintedBlockState();
				BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(st);
				if(model instanceof FabricBakedModel)
					((FabricBakedModel)model).emitBlockQuads(blockView, st, pos, randomSupplier, context);
				else
					context.fallbackConsumer().accept(model);
				return;
			} catch (Exception e) {
			}
		}

		QuadEmitter emitter = context.getEmitter();

		for(Direction direction : Direction.values()) {
			// Add a new face to the mesh
			emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
			// Set the sprite of the face, must be called after .square()
			// We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
			emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
			// Enable texture usage
			emitter.spriteColor(0, -1, -1, -1, -1);
			// Add the quad to the mesh
			emitter.emit();
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

	}

	@Override
	public ModelOverrideList getOverrides() {
		return null;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState arg0, Direction arg1, Random arg2) {
		return fallback.getQuads(arg0, arg1, arg2);
	}

	@Override
	public Sprite getParticleSprite() {
		return sprite;
	}

	@Override
	public ModelTransformation getTransformation() {
		return null;
	}

	@Override
	public boolean hasDepth() {
		return false;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public boolean isSideLit() {
		return false;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		sprite = textureGetter.apply(ID);
		fallback = loader.bake(FALLBACK_ID, rotationContainer);
		return this;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		return Arrays.asList(ID);
	}

}
