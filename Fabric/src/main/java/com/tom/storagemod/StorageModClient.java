package com.tom.storagemod;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.client.UnbakedPaintedModel;
import com.tom.storagemod.network.DataPacket;
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
import com.tom.storagemod.util.IDataReceiver;

import io.netty.buffer.ByteBufInputStream;

public class StorageModClient implements ClientModInitializer {
	public static KeyMapping openTerm;

	@Override
	public void onInitializeClient() {
		MenuScreens.register(Content.storageTerminalMenu.get(), StorageTerminalScreen::new);
		MenuScreens.register(Content.craftingTerminalMenu.get(), CraftingTerminalScreen::new);
		MenuScreens.register(Content.invConfigMenu.get(), InventoryConfiguratorScreen::new);
		MenuScreens.register(Content.levelEmitterMenu.get(), LevelEmitterScreen::new);
		MenuScreens.register(Content.inventoryLink.get(), InventoryLinkScreen::new);
		MenuScreens.register(Content.itemFilterMenu.get(), ItemFilterScreen::new);
		MenuScreens.register(Content.tagItemFilterMenu.get(), TagItemFilterScreen::new);
		MenuScreens.register(Content.filingCabinetMenu.get(), FilingCabinetScreen::new);

		BlockRenderLayerMap.INSTANCE.putBlock(Content.paintedTrim.get(), RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Content.invCableFramed.get(), RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Content.levelEmitter.get(), RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(Content.invCableConnectorFramed.get(), RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putBlock(Content.invProxy.get(), RenderType.translucent());

		ClientPlayNetworking.registerGlobalReceiver(DataPacket.ID, (p, c) -> {
			if(Minecraft.getInstance().screen instanceof IDataReceiver d) {
				d.receive(p.tag());
			}
		});

		ModelLoadingPlugin.register(new ModelLoadingPlugin() {
			private Set<BlockState> states = new HashSet<>();

			@Override
			public void initialize(Context ctx) {
				bakeModels(states);
				ctx.modifyBlockModelOnLoad().register((p, c) -> {
					if (states.contains(c.state()) && !(p instanceof UnbakedPaintedModel)) {
						return new UnbakedPaintedModel(p);
					}
					return p;
				});
			}
		});

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			if (world != null) {
				try {
					BlockState mimicBlock = ((PaintedBlockEntity)world.getBlockEntity(pos)).getPaintedBlockState();
					return Minecraft.getInstance().getBlockColors().getColor(mimicBlock, world, pos, tintIndex);
				} catch (Exception var8) {
					return -1;
				}
			}
			return -1;
		}, Content.paintedTrim.get(), Content.invCableFramed.get(), Content.invCableConnectorFramed.get(), Content.invProxy.get());

		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hr) -> {
			PoseStack ps = new PoseStack();
			ps.pushPose();
			ClientUtil.drawTerminalOutline(ps);
			ClientUtil.drawConfiguratorOutline(ps);
			ps.popPose();
			return true;
		});

		ClientLoginNetworking.registerGlobalReceiver(StorageMod.id("config"), (mc, handler, buf, fc) -> {
			Config read;
			try (InputStreamReader reader = new InputStreamReader(new ByteBufInputStream(buf))){
				read = StorageMod.gson.fromJson(reader, Config.class);
			} catch (Exception e) {
				StorageMod.LOGGER.warn("Error loading server config", e);
				return CompletableFuture.completedFuture(null);
			}
			StorageMod.CONFIG = read;
			StorageMod.LOGGER.info("Received server config");
			return CompletableFuture.completedFuture(PacketByteBufs.empty());
		});

		openTerm = new KeyMapping("key.toms_storage.open_terminal", InputConstants.KEY_B, KeyMapping.CATEGORY_GAMEPLAY);
		KeyBindingHelper.registerKeyBinding(openTerm);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null)
				return;

			if(openTerm.consumeClick()) {
				NetworkHandler.openTerminal();
			}
		});

		ItemTooltipCallback.EVENT.register((s, t, c, l) -> {
			ClientUtil.collectExtraTooltips(s, l);
		});

		//if (StorageMod.polymorph)PolymorphTerminalWidget.register();
	}

	private static void bakeModels(Set<BlockState> states) {
		bindPaintedModel(states, Content.paintedTrim);
		bindPaintedModel(states, Content.invCableFramed);
		bindPaintedModel(states, Content.invProxy);
		bindPaintedModel(states, Content.invCableConnectorFramed);
	}

	private static void bindPaintedModel(Set<BlockState> states, GameObject<? extends Block> block) {
		states.addAll(block.get().getStateDefinition().getPossibleStates());
	}

	public static Supplier<RenderPipeline> registerPipeline(Supplier<RenderPipeline> factory) {
		var p = factory.get();
		RenderPipelines.register(p);
		if (FabricLoader.getInstance().isModLoaded("iris"))
			IrisPipelines.assignPipeline(p, ShaderKey.LINES);
		return () -> p;
	}
}
