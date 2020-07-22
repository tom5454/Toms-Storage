package com.tom.storagemod.model;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import com.tom.fabriclibs.client.DynamicBakedModel;
import com.tom.fabriclibs.client.IModelData;
import com.tom.fabriclibs.client.IModelData.ModelData;
import com.tom.storagemod.tile.TileEntityPainted;

public class BakedPaintedModel implements DynamicBakedModel {
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
	public boolean hasDepth() {
		return false;
	}

	@Override
	public boolean isSideLit() {
		return false;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public Sprite getSprite() {
		return parent.getSprite();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData modelData) {
		BakedModel model = null;
		Supplier<BlockState> blockstateSupp = modelData.getData(TileEntityPainted.FACADE_STATE);
		BlockState blockstate = null;
		if(blockstateSupp != null)blockstate = blockstateSupp.get();
		//RenderLayer layer = MinecraftForgeClient.getRenderLayer();
		if (blockstate == null || blockstate == Blocks.AIR.getDefaultState()) {
			blockstate = state;
			model = parent;
			//if(layer != null && layer != RenderLayer.getSolid())return Collections.emptyList();
		}
		/*if (layer != null && ! RenderTypeLookup.canRenderInLayer(blockstate, layer)) { // always render in the null layer or the block-breaking textures don't show up
			return Collections.emptyList();
		}*/
		if(model == null)
			model = MinecraftClient.getInstance().getBlockRenderManager().getModel(blockstate);
		return model.getQuads(blockstate, side, rand);
	}

	@Override
	public IModelData getModelData(BlockRenderView world, BlockPos pos, BlockState state) {
		BlockEntity te = world.getBlockEntity(pos);
		return new ModelData().set(TileEntityPainted.FACADE_STATE, te instanceof TileEntityPainted ? ((TileEntityPainted)te)::getPaintedBlockState : null);
	}

	@Override
	public ModelTransformation getTransformation() {
		return ModelTransformation.NONE;
	}
}
