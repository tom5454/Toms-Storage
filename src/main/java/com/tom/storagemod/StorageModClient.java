package com.tom.storagemod;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.storagemod.gui.GuiCraftingTerminal;
import com.tom.storagemod.gui.GuiFiltered;
import com.tom.storagemod.gui.GuiInventoryLink;
import com.tom.storagemod.gui.GuiLevelEmitter;
import com.tom.storagemod.gui.GuiStorageTerminal;
import com.tom.storagemod.item.ItemWirelessTerminal;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.TileEntityPainted;

public class StorageModClient {
	public static KeyMapping openTerm;

	public static void clientSetup() {
		MenuScreens.register(StorageMod.storageTerminal, GuiStorageTerminal::new);
		MenuScreens.register(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);
		MenuScreens.register(StorageMod.filteredConatiner, GuiFiltered::new);
		MenuScreens.register(StorageMod.levelEmitterConatiner, GuiLevelEmitter::new);
		MenuScreens.register(StorageMod.inventoryLink, GuiInventoryLink::new);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(StorageModClient::bakeModels);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.paintedTrim, e -> true);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.invCableFramed, e -> true);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.invProxy, e -> true);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.invCableConnectorFramed, e -> true);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.levelEmitter, RenderType.cutout());
		BlockColors colors = Minecraft.getInstance().getBlockColors();
		colors.register((state, world, pos, tintIndex) -> {
			if (world != null) {
				try {
					BlockState mimicBlock = ((TileEntityPainted)world.getBlockEntity(pos)).getPaintedBlockState();
					return colors.getColor(mimicBlock, world, pos, tintIndex);
				} catch (Exception var8) {
					return - 1;
				}
			}
			return -1;
		}, StorageMod.paintedTrim, StorageMod.invCableFramed, StorageMod.invProxy, StorageMod.invCableConnectorFramed);
		openTerm = new KeyMapping("key.toms_storage.open_terminal", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_B), KeyMapping.CATEGORY_GAMEPLAY);
		ClientRegistry.registerKeyBinding(openTerm);
		MinecraftForge.EVENT_BUS.register(StorageModClient.class);
	}

	private static void bakeModels(ModelBakeEvent event) {
		bindPaintedModel(event, StorageMod.paintedTrim);
		bindPaintedModel(event, StorageMod.invCableFramed);
		bindPaintedModel(event, StorageMod.invProxy);
		bindPaintedModel(event, StorageMod.invCableConnectorFramed);
	}

	private static void bindPaintedModel(ModelBakeEvent event, Block blockFor) {
		ResourceLocation baseLoc = blockFor.delegate.name();
		blockFor.getStateDefinition().getPossibleStates().forEach(st -> {
			ModelResourceLocation resLoc = BlockModelShaper.stateToModelLocation(baseLoc, st);
			event.getModelRegistry().put(resLoc, new BakedPaintedModel(blockFor, event.getModelRegistry().get(resLoc)));
		});
	}

	@SubscribeEvent
	public static void renderWorldLastEvent(RenderLevelLastEvent evt) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if( player == null )
			return;

		if(!ItemWirelessTerminal.isPlayerHolding(player))
			return;

		BlockHitResult lookingAt = (BlockHitResult) player.pick(Config.wirelessRange, 0f, true);
		BlockState state = mc.level.getBlockState(lookingAt.getBlockPos());
		if(state.is(StorageTags.REMOTE_ACTIVATE)) {
			BlockPos pos = lookingAt.getBlockPos();
			Vec3 renderPos = mc.gameRenderer.getMainCamera().getPosition();
			PoseStack ms = evt.getPoseStack();
			VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
			drawShape(ms, buf, state.getOcclusionShape(player.level, pos), pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z, 1, 1, 1, 0.4f);
			mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
		}
	}

	private static void drawShape(PoseStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
		PoseStack.Pose entry = matrices.last();
		voxelShape.forAllEdges((k, l, m, n, o, p) -> {
			float q = (float)(n - k);
			float r = (float)(o - l);
			float s = (float)(p - m);
			float t = Mth.sqrt(q * q + r * r + s * s);
			q /= t;
			r /= t;
			s /= t;
			vertexConsumer.vertex(entry.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(entry.normal(), q, r, s).endVertex();
			vertexConsumer.vertex(entry.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(entry.normal(), q, r, s).endVertex();
		});
	}

	public static void tooltip(String key, List<Component> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean addShift, List<Component> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.get("tooltip.toms_storage." + key, args).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(new TextComponent(sp[i]));
			}
		} else if(addShift) {
			tooltip.add(new TranslatableComponent("tooltip.toms_storage.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent evt) {
		if (Minecraft.getInstance().player == null || evt.phase == Phase.START)
			return;

		if(openTerm.consumeClick()) {
			NetworkHandler.openTerminal();
		}
	}
}
