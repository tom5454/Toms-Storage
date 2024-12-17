package com.tom.storagemod.client;

import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;

public class UnbakedPaintedModel implements UnbakedBlockStateModel {
	private UnbakedBlockStateModel parent;

	public UnbakedPaintedModel(UnbakedBlockStateModel parent) {
		this.parent = parent;
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.parent.resolveDependencies(resolver);
	}

	@Override
	public BakedModel bake(ModelBaker modelBaker) {
		return new BakedPaintedModel(parent.bake(modelBaker));
	}

	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		return parent.visualEqualityGroup(blockState);
	}
}
