package com.tom.storagemod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;

import com.tom.storagemod.client.BakedPaintedModel;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.client.CustomRenderTypes;
import com.tom.storagemod.client.PaintedExtension;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.platform.GameObject;
import com.tom.storagemod.screen.CraftingTerminalScreen;
import com.tom.storagemod.screen.FilingCabinetScreen;
import com.tom.storagemod.screen.InventoryConfiguratorScreen;
import com.tom.storagemod.screen.InventoryLinkScreen;
import com.tom.storagemod.screen.ItemFilterScreen;
import com.tom.storagemod.screen.LevelEmitterScreen;
import com.tom.storagemod.screen.StorageTerminalScreen;
import com.tom.storagemod.screen.TagItemFilterScreen;

public class StorageModClient {
	public static KeyMapping openTerm;
	public static List<RenderPipeline> pipelines = new ArrayList<>();
	public static final Identifier CONFIGURATOR_OVERLAY_ID = Identifier.tryBuild(StorageMod.modid, "configurator_info");

	public static void preInit(ModContainer mc, IEventBus bus) {
		bus.addListener(StorageModClient::initKeybinds);
		bus.addListener(StorageModClient::bakeModels);
		bus.addListener(StorageModClient::registerScreens);
		bus.addListener(StorageModClient::registerPipelines);
		bus.addListener(StorageModClient::registerOverlays);
		bus.addListener(StorageModClient::registerClientExtensions);
		mc.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	private static void registerScreens(RegisterMenuScreensEvent e) {
		e.register(Content.storageTerminalMenu.get(), StorageTerminalScreen::new);
		e.register(Content.craftingTerminalMenu.get(), CraftingTerminalScreen::new);
		e.register(Content.levelEmitterMenu.get(), LevelEmitterScreen::new);
		e.register(Content.inventoryLink.get(), InventoryLinkScreen::new);
		e.register(Content.itemFilterMenu.get(), ItemFilterScreen::new);
		e.register(Content.tagItemFilterMenu.get(), TagItemFilterScreen::new);
		e.register(Content.invConfigMenu.get(), InventoryConfiguratorScreen::new);
		e.register(Content.filingCabinetMenu.get(), FilingCabinetScreen::new);
	}

	public static void clientSetup() {
		NeoForge.EVENT_BUS.register(StorageModClient.class);
	}

	public static void registerOverlays(RegisterGuiLayersEvent event) {
		event.registerAboveAll(CONFIGURATOR_OVERLAY_ID, StorageModClient::renderConfiguratorOverlay);
	}

	public static void renderConfiguratorOverlay(GuiGraphicsExtractor gr, DeltaTracker p_348559_) {
		ClientUtil.drawConfiguratorOverlay(gr);
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent ev) {
		ev.registerBlock(new PaintedExtension(), Content.paintedTrim.get(), Content.invCableFramed.get(), Content.invProxy.get(), Content.invCableConnectorFramed.get());
	}

	@SubscribeEvent
	public static void getTooltip(ItemTooltipEvent evt) {
		ClientUtil.collectExtraTooltips(evt.getItemStack(), evt.getToolTip());
	}

	private static void initKeybinds(RegisterKeyMappingsEvent evt) {
		openTerm = new KeyMapping("key.toms_storage.open_terminal", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_B), KeyMapping.Category.GAMEPLAY);
		evt.register(openTerm);
	}

	private static void bakeModels(ModelEvent.ModifyBakingResult event) {
		bindPaintedModel(event, Content.paintedTrim);
		bindPaintedModel(event, Content.invCableFramed);
		bindPaintedModel(event, Content.invProxy);
		bindPaintedModel(event, Content.invCableConnectorFramed);
	}

	private static void bindPaintedModel(ModelEvent.ModifyBakingResult event, GameObject<? extends Block> block) {
		block.get().getStateDefinition().getPossibleStates().forEach(st -> {
			var models = event.getBakingResult().blockStateModels();
			models.put(st, new BakedPaintedModel(block.get(), models.get(st)));
		});
	}

	@SubscribeEvent
	public static void renderWorldOutline(SubmitCustomGeometryEvent evt) {
		ClientUtil.drawTerminalOutline(evt.getPoseStack(), evt.getSubmitNodeCollector());
		ClientUtil.drawConfiguratorOutline(evt.getPoseStack(), evt.getSubmitNodeCollector());
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent.Post evt) {
		if (Minecraft.getInstance().player == null)
			return;

		if(openTerm.consumeClick()) {
			NetworkHandler.openTerminal();
		}
	}

	private static void registerPipelines(RegisterRenderPipelinesEvent event) {
		CustomRenderTypes.linesNoDepth();//Class init
		pipelines.forEach(event::registerPipeline);
	}

	public static Supplier<RenderPipeline> registerPipeline(Supplier<RenderPipeline> factory) {
		var pipeline = factory.get();
		pipelines.add(pipeline);
		if (ModList.get().isLoaded("iris"))
			IrisPipelines.assignPipeline(pipeline, ShaderKey.LINES);
		return () -> pipeline;
	}
}
