package com.tom.storagemod.client;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;

public class UnbakedPaintedModel implements BlockStateModel.UnbakedRoot {
	private BlockStateModel.UnbakedRoot parent;

	public UnbakedPaintedModel(BlockStateModel.UnbakedRoot parent) {
		this.parent = parent;
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.parent.resolveDependencies(resolver);
	}

	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		return parent.visualEqualityGroup(blockState);
	}

	@Override
	public BlockStateModel bake(BlockState blockState, ModelBaker modelBaker) {
		return new BakedPaintedModel(parent.bake(blockState, modelBaker));
	}
}
