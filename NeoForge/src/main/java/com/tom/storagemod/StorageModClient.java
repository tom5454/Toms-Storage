package com.tom.storagemod;

import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.mojang.blaze3d.platform.InputConstants;

import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.client.BakedPaintedModel;
import com.tom.storagemod.client.ClientUtil;
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

	public static void preInit(ModContainer mc, IEventBus bus) {
		bus.addListener(StorageModClient::registerColors);
		bus.addListener(StorageModClient::initKeybinds);
		bus.addListener(StorageModClient::bakeModels);
		bus.addListener(StorageModClient::registerScreens);

		try {
			mc.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		} catch (Throwable e) {
		}
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

	@SubscribeEvent
	public static void getTooltip(ItemTooltipEvent evt) {
		ClientUtil.collectExtraTooltips(evt.getItemStack(), evt.getToolTip());
	}

	private static void initKeybinds(RegisterKeyMappingsEvent evt) {
		openTerm = new KeyMapping("key.toms_storage.open_terminal", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_B), KeyMapping.CATEGORY_GAMEPLAY);
		evt.register(openTerm);
	}

	private static void registerColors(RegisterColorHandlersEvent.Block event) {
		event.register(StorageModClient::getColor, Content.paintedTrim.get(), Content.invCableFramed.get(), Content.invProxy.get(), Content.invCableConnectorFramed.get());
	}

	private static int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
		if (world != null) {
			try {
				BlockState mimicBlock = ((PaintedBlockEntity)world.getBlockEntity(pos)).getPaintedBlockState();
				return Minecraft.getInstance().getBlockColors().getColor(mimicBlock, world, pos, tintIndex);
			} catch (Exception var8) {
				return -1;
			}
		}
		return -1;
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

	@SubscribeEvent
	public static void renderWorldOutline(RenderLevelStageEvent evt) {
		if(evt.getStage() == Stage.AFTER_PARTICLES) {
			ClientUtil.drawTerminalOutline(evt.getPoseStack());
			ClientUtil.drawConfiguratorOutline(evt.getPoseStack());
		}
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent.Post evt) {
		if (Minecraft.getInstance().player == null)
			return;

		if(openTerm.consumeClick()) {
			NetworkHandler.openTerminal();
		}
	}
}
