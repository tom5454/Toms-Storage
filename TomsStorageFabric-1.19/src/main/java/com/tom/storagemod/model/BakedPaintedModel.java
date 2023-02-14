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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Pair;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.tile.PaintedBlockEntity;

public class BakedPaintedModel implements UnbakedModel, BakedModel, FabricBakedModel {
	private static final Material ID = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(StorageMod.modid, "block/trim"));
	private static final ResourceLocation FALLBACK_ID = new ResourceLocation("minecraft:block/stone");
	private TextureAtlasSprite sprite;
	private BakedModel fallback;
	public BakedPaintedModel() {
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
			try {
				BlockState st = ((PaintedBlockEntity)tile).getPaintedBlockState();
				BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(st);
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
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {

	}

	@Override
	public ItemOverrides getOverrides() {
		return null;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState arg0, Direction arg1, RandomSource arg2) {
		return fallback.getQuads(arg0, arg1, arg2);
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return sprite;
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

	@Override
	public BakedModel bake(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId) {
		sprite = textureGetter.apply(ID);
		fallback = loader.bake(FALLBACK_ID, rotationContainer);
		return this;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		return Arrays.asList(ID);
	}

}
