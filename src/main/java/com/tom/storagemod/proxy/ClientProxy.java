package com.tom.storagemod.proxy;

import java.util.List;

import net.minecraft.ChatFormatting;
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

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.gui.GuiCraftingTerminal;
import com.tom.storagemod.gui.GuiFiltered;
import com.tom.storagemod.gui.GuiLevelEmitter;
import com.tom.storagemod.gui.GuiStorageTerminal;
import com.tom.storagemod.item.ItemWirelessTerminal;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.tile.TileEntityPainted;

public class ClientProxy implements IProxy {

	@Override
	public void setup() {

	}

	@Override
	public void clientSetup() {
		MenuScreens.register(StorageMod.storageTerminal, GuiStorageTerminal::new);
		MenuScreens.register(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);
		MenuScreens.register(StorageMod.filteredConatiner, GuiFiltered::new);
		MenuScreens.register(StorageMod.levelEmitterConatiner, GuiLevelEmitter::new);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::bakeModels);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.paintedTrim, e -> true);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.invCableFramed, e -> true);
		ItemBlockRenderTypes.setRenderLayer(StorageMod.invProxy, e -> true);
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
		}, StorageMod.paintedTrim, StorageMod.invCableFramed, StorageMod.invProxy);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::renderWorldLastEvent);
	}

	private static void bakeModels(ModelBakeEvent event) {
		bindPaintedModel(event, StorageMod.paintedTrim);
		bindPaintedModel(event, StorageMod.invCableFramed);
		bindPaintedModel(event, StorageMod.invProxy);
	}

	private static void bindPaintedModel(ModelBakeEvent event, Block blockFor) {
		ResourceLocation baseLoc = blockFor.delegate.name();
		blockFor.getStateDefinition().getPossibleStates().forEach(st -> {
			ModelResourceLocation resLoc = BlockModelShaper.stateToModelLocation(baseLoc, st);
			event.getModelRegistry().put(resLoc, new BakedPaintedModel(blockFor, event.getModelRegistry().get(resLoc)));
		});
	}

	private static void renderWorldLastEvent(RenderWorldLastEvent evt) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if( player == null )
			return;

		if(!ItemWirelessTerminal.isPlayerHolding(player))
			return;

		BlockHitResult lookingAt = (BlockHitResult) player.pick(Config.wirelessRange, 0f, true);
		BlockState state = mc.level.getBlockState(lookingAt.getBlockPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			BlockPos pos = lookingAt.getBlockPos();
			Vec3 renderPos = mc.gameRenderer.getMainCamera().getPosition();
			PoseStack ms = evt.getMatrixStack();
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

	public static void tooltip(String key, List<Component> tooltip) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.get("tooltip.toms_storage." + key).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(new TextComponent(sp[i]));
			}
		} else {
			tooltip.add(new TranslatableComponent("tooltip.toms_storage.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}
}
