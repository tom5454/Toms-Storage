package com.tom.storagemod;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

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
	protected static final Identifier PAINT = new Identifier(StorageMod.modid, "paint");

	@Override
	public void onInitializeClient() {
		HandledScreens.register(StorageMod.storageTerminal, StorageTerminalScreen::new);
		HandledScreens.register(StorageMod.craftingTerminalCont, CraftingTerminalScreen::new);
		HandledScreens.register(StorageMod.filteredConatiner, FilteredScreen::new);
		HandledScreens.register(StorageMod.levelEmitterConatiner, LevelEmitterScreen::new);
		HandledScreens.register(StorageMod.inventoryLink, InventoryLinkScreen::new);

		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.paintedTrim, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCablePainted, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.levelEmitter, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCableConnectorPainted, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invProxyPainted, RenderLayer.getTranslucent());

		ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_S2C, (mc, h, buf, rp) -> {
			NbtCompound tag = buf.readUnlimitedNbt();
			mc.submit(() -> {
				if(mc.currentScreen instanceof IDataReceiver) {
					((IDataReceiver)mc.currentScreen).receive(tag);
				}
			});
		});

		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ModelResourceProvider() {

			@Override
			public UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context)
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
					return MinecraftClient.getInstance().getBlockColors().getColor(mimicBlock, world, pos, tintIndex);
				} catch (Exception var8) {
					return -1;
				}
			}
			return -1;
		}, StorageMod.paintedTrim, StorageMod.invCablePainted, StorageMod.invCableConnectorPainted, StorageMod.invProxyPainted);

		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hr) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			PlayerEntity player = mc.player;
			if( player == null )
				return true;

			if(!WirelessTerminalItem.isPlayerHolding(player))
				return true;

			BlockHitResult lookingAt = (BlockHitResult) player.raycast(StorageMod.CONFIG.wirelessRange, 0f, true);
			BlockState state = mc.world.getBlockState(lookingAt.getBlockPos());
			if(state.isIn(StorageTags.REMOTE_ACTIVATE)) {
				BlockPos pos = lookingAt.getBlockPos();
				Vec3d renderPos = mc.gameRenderer.getCamera().getPos();
				VertexConsumer buf = mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getLines());
				drawShapeOutline(ctx.matrixStack(), buf, state.getCollisionShape(player.world, pos), pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z, 1, 1, 1, 0.4f);
				mc.getBufferBuilders().getEntityVertexConsumers().draw(RenderLayer.getLines());
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
	}

	private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
		MatrixStack.Entry entry = matrices.peek();
		voxelShape.forEachEdge((k, l, m, n, o, p) -> {
			float q = (float)(n - k);
			float r = (float)(o - l);
			float s = (float)(p - m);
			float t = MathHelper.sqrt(q * q + r * r + s * s);
			q /= t;
			r /= t;
			s /= t;
			vertexConsumer.vertex(entry.getPositionMatrix(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(entry.getNormalMatrix(), q, r, s).next();
			vertexConsumer.vertex(entry.getPositionMatrix(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(entry.getNormalMatrix(), q, r, s).next();
		});
	}

	public static void tooltip(String key, List<Text> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean shift, List<Text> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.translate("tooltip.toms_storage." + key, args).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(Text.literal(sp[i]));
			}
		} else if(shift) {
			tooltip.add(Text.translatable("tooltip.toms_storage.hold_shift_for_info").formatted(Formatting.ITALIC, Formatting.GRAY));
		}
	}
}
