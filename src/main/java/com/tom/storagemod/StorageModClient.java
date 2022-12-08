package com.tom.storagemod;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.gui.CraftingTerminalScreen;
import com.tom.storagemod.gui.FilteredScreen;
import com.tom.storagemod.gui.InventoryLinkScreen;
import com.tom.storagemod.gui.LevelEmitterScreen;
import com.tom.storagemod.gui.StorageTerminalScreen;
import com.tom.storagemod.item.WirelessTerminalItem;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.tile.PaintedBlockEntity;

import io.netty.buffer.ByteBufInputStream;

public class StorageModClient implements ClientModInitializer {
	protected static final ResourceLocation PAINT = new ResourceLocation(StorageMod.modid, "paint");
	public static KeyMapping openTerm;

	@Override
	public void onInitializeClient() {
		MenuScreens.register(StorageMod.storageTerminal, StorageTerminalScreen::new);
		MenuScreens.register(StorageMod.craftingTerminalCont, CraftingTerminalScreen::new);
		MenuScreens.register(StorageMod.filteredConatiner, FilteredScreen::new);
		MenuScreens.register(StorageMod.levelEmitterConatiner, LevelEmitterScreen::new);
		MenuScreens.register(StorageMod.inventoryLink, InventoryLinkScreen::new);

		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.paintedTrim, RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCablePainted, RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.levelEmitter, RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCableConnectorPainted, RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invProxyPainted, RenderType.translucent());

		ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_S2C, (mc, h, buf, rp) -> {
			CompoundTag tag = buf.readAnySizeNbt();
			mc.submit(() -> {
				if(mc.screen instanceof IDataReceiver) {
					((IDataReceiver)mc.screen).receive(tag);
				}
			});
		});

		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ModelResourceProvider() {

			@Override
			public UnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context)
					throws ModelProviderException {
				if(resourceId.equals(PAINT)) {
					return new BakedPaintedModel();
				}
				return null;
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
		}, StorageMod.paintedTrim, StorageMod.invCablePainted, StorageMod.invCableConnectorPainted, StorageMod.invProxyPainted);

		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hr) -> {
			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			if( player == null )
				return true;

			if(!WirelessTerminalItem.isPlayerHolding(player))
				return true;

			BlockHitResult lookingAt = (BlockHitResult) player.pick(StorageMod.CONFIG.wirelessRange, 0f, true);
			BlockState state = mc.level.getBlockState(lookingAt.getBlockPos());
			if(state.is(StorageTags.REMOTE_ACTIVATE)) {
				BlockPos pos = lookingAt.getBlockPos();
				Vec3 renderPos = mc.gameRenderer.getMainCamera().getPosition();
				VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
				drawShapeOutline(ctx.matrixStack(), buf, state.getCollisionShape(player.level, pos), pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z, 1, 1, 1, 0.4f);
				mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
				return false;
			}

			return true;
		});

		try {
			Class<?> clz = Class.forName("com.kqp.inventorytabs.api.TabProviderRegistry");
			Method regSimpleBlock = clz.getDeclaredMethod("registerSimpleBlock", Block.class);
			regSimpleBlock.invoke(null, StorageMod.terminal);
			regSimpleBlock.invoke(null, StorageMod.craftingTerminal);
		} catch (Throwable e) {
		}

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
	}

	private static void drawShapeOutline(PoseStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
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

	public static void tooltip(String key, boolean shift, List<Component> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.get("tooltip.toms_storage." + key, args).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(Component.literal(sp[i]));
			}
		} else if(shift) {
			tooltip.add(Component.translatable("tooltip.toms_storage.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}
}
