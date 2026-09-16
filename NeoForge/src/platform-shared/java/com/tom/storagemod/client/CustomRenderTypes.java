package com.tom.storagemod.client;

import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;

public class CustomRenderTypes {
	public static final Supplier<RenderPipeline> LINES = StorageModClient.registerPipeline(() -> {
		return RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
				.withLocation(Identifier.tryBuild(StorageMod.modid, "pipeline/lines"))
				.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
				.withColorTargetState(ColorTargetState.DEFAULT)
				.build();
	});

	private static final RenderType LINES_NO_DEPTH = RenderType.create(StorageMod.modid + ":lines_no_depth",
			RenderSetup.builder(LINES.get()).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.createRenderSetup());

	public static RenderType linesNoDepth() {
		return LINES_NO_DEPTH;
	}
}
