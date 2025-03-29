package com.tom.storagemod.client;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderType.create;

import java.util.OptionalDouble;
import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;

public class CustomRenderTypes {
	public static final Supplier<RenderPipeline> LINES = StorageModClient.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation(ResourceLocation.tryBuild(StorageMod.modid, "pipeline/lines"))
				.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
				.build();
	});

	public static RenderType linesNoDepth() {
		return create(
				StorageMod.modid + ":lines_no_depth",
				1536,
				LINES.get(),
				RenderType.CompositeState.builder().
				setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).
				setLayeringState(VIEW_OFFSET_Z_LAYERING).
				setOutputState(ITEM_ENTITY_TARGET).
				createCompositeState(false));
	}
}
