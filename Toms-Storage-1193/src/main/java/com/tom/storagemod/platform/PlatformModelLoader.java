package com.tom.storagemod.platform;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tom.storagemod.Content;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.util.GameObject;

public class PlatformModelLoader {

	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(PlatformModelLoader::bakeModels);
	}

	private static void bakeModels(ModelEvent.ModifyBakingResult event) {
		bindPaintedModel(event, Content.paintedTrim);
		bindPaintedModel(event, Content.invCableFramed);
		bindPaintedModel(event, Content.invProxy);
		bindPaintedModel(event, Content.invCableConnectorFramed);
	}

	private static void bindPaintedModel(ModelEvent.ModifyBakingResult event, GameObject<? extends Block> block) {
		ResourceLocation baseLoc = block.getId();
		block.get().getStateDefinition().getPossibleStates().forEach(st -> {
			ModelResourceLocation resLoc = BlockModelShaper.stateToModelLocation(baseLoc, st);
			event.getModels().put(resLoc, new BakedPaintedModel(block.get(), event.getModels().get(resLoc)));
		});
	}
}
