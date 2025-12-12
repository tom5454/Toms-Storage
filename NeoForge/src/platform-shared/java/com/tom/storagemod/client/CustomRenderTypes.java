package com.tom.storagemod.client;


import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;

public class CustomRenderTypes {
	public static final Supplier<RenderPipeline> LINES = StorageModClient.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation(Identifier.tryBuild(StorageMod.modid, "pipeline/lines"))
				.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
				.build();
	});

	private static final RenderType LINES_NO_DEPTH = RenderType.create(StorageMod.modid + ":lines_no_depth",
			RenderSetup.builder(LINES.get()).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET).createRenderSetup());

	public static RenderType linesNoDepth() {
		return LINES_NO_DEPTH;
	}
}
