package com.tom.storagemod.proxy;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.gui.GuiCraftingTerminal;
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
		ScreenManager.registerFactory(StorageMod.storageTerminal, GuiStorageTerminal::new);
		ScreenManager.registerFactory(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::bakeModels);
		RenderTypeLookup.setRenderLayer(StorageMod.paintedTrim, e -> true);
		RenderTypeLookup.setRenderLayer(StorageMod.invCableFramed, e -> true);
		RenderTypeLookup.setRenderLayer(StorageMod.invProxy, e -> true);
		BlockColors colors = Minecraft.getInstance().getBlockColors();
		colors.register((state, world, pos, tintIndex) -> {
			if (world != null) {
				try {
					BlockState mimicBlock = ((TileEntityPainted)world.getTileEntity(pos)).getPaintedBlockState();
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
		blockFor.getStateContainer().getValidStates().forEach(st -> {
			ModelResourceLocation resLoc = BlockModelShapes.getModelLocation(baseLoc, st);
			event.getModelRegistry().put(resLoc, new BakedPaintedModel(blockFor, event.getModelRegistry().get(resLoc)));
		});
	}

	private static void renderWorldLastEvent(RenderWorldLastEvent evt) {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		if( player == null )
			return;

		if(!ItemWirelessTerminal.isPlayerHolding(player))
			return;

		BlockRayTraceResult lookingAt = (BlockRayTraceResult) player.pick(Config.wirelessRange, 0f, true);
		BlockState state = mc.world.getBlockState(lookingAt.getPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			BlockPos pos = lookingAt.getPos();
			Vector3d renderPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
			Tessellator.getInstance().getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			MatrixStack ms = evt.getMatrixStack();
			ms.translate(pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z);
			float scale = 1.01f;
			ms.scale(scale, scale, scale);
			ms.translate(-0.001f, -0.001f, -0.001f);
			drawShape(ms, Tessellator.getInstance().getBuffer(), state.getRenderShape(player.world, pos), 0, 0, 0, 1, 1, 1, 1);
			Tessellator.getInstance().draw();
		}
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		shapeIn.forEachEdge((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
			bufferIn.pos(matrix4f, (float)(p_230013_12_ + xIn), (float)(p_230013_14_ + yIn), (float)(p_230013_16_ + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(matrix4f, (float)(p_230013_18_ + xIn), (float)(p_230013_20_ + yIn), (float)(p_230013_22_ + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

	public static void tooltip(String key, List<ITextComponent> tooltip) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.format("tooltip.toms_storage." + key).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(new StringTextComponent(sp[i]));
			}
		} else {
			tooltip.add(new TranslationTextComponent("tooltip.toms_storage.hold_shift_for_info").mergeStyle(TextFormatting.ITALIC, TextFormatting.GRAY));
		}
	}
}
