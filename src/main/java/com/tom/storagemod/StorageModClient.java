package com.tom.storagemod;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.storagemod.gui.CraftingTerminalScreen;
import com.tom.storagemod.gui.FilteredScreen;
import com.tom.storagemod.gui.InventoryLinkScreen;
import com.tom.storagemod.gui.LevelEmitterScreen;
import com.tom.storagemod.gui.StorageTerminalScreen;
import com.tom.storagemod.item.WirelessTerminalItem;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.PaintedBlockEntity;

public class StorageModClient {
	public static KeyMapping openTerm;

	public static void preInit() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(StorageModClient::registerColors);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(StorageModClient::bakeModels);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(StorageModClient::initKeybinds);
	}

	public static void clientSetup() {
		MenuScreens.register(StorageMod.storageTerminal.get(), StorageTerminalScreen::new);
		MenuScreens.register(StorageMod.craftingTerminalCont.get(), CraftingTerminalScreen::new);
		MenuScreens.register(StorageMod.filteredConatiner.get(), FilteredScreen::new);
		MenuScreens.register(StorageMod.levelEmitterConatiner.get(), LevelEmitterScreen::new);
		MenuScreens.register(StorageMod.inventoryLink.get(), InventoryLinkScreen::new);
		MinecraftForge.EVENT_BUS.register(StorageModClient.class);
	}

	public static void initKeybinds(RegisterKeyMappingsEvent evt) {
		openTerm = new KeyMapping("key.toms_storage.open_terminal", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_B), KeyMapping.CATEGORY_GAMEPLAY);
		evt.register(openTerm);
	}

	public static void registerColors(RegisterColorHandlersEvent.Block event) {
		event.register(StorageModClient::getColor, StorageMod.paintedTrim.get(), StorageMod.invCableFramed.get(), StorageMod.invProxy.get(), StorageMod.invCableConnectorFramed.get());
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

	private static void bakeModels(ModelEvent.BakingCompleted event) {
		bindPaintedModel(event, StorageMod.paintedTrim);
		bindPaintedModel(event, StorageMod.invCableFramed);
		bindPaintedModel(event, StorageMod.invProxy);
		bindPaintedModel(event, StorageMod.invCableConnectorFramed);
	}

	private static void bindPaintedModel(ModelEvent.BakingCompleted event, RegistryObject<? extends Block> block) {
		ResourceLocation baseLoc = block.getId();
		block.get().getStateDefinition().getPossibleStates().forEach(st -> {
			ModelResourceLocation resLoc = BlockModelShaper.stateToModelLocation(baseLoc, st);
			event.getModels().put(resLoc, new BakedPaintedModel(block.get(), event.getModels().get(resLoc)));
		});
	}

	@SubscribeEvent
	public static void renderWorldOutline(RenderLevelStageEvent evt) {
		if(evt.getStage() == Stage.AFTER_PARTICLES) {
			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			if( player == null )
				return;

			if(!WirelessTerminalItem.isPlayerHolding(player))
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
				tooltip.add(Component.literal(sp[i]));
			}
		} else if(addShift) {
			tooltip.add(Component.translatable("tooltip.toms_storage.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
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
