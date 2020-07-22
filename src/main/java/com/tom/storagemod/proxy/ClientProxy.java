package com.tom.storagemod.proxy;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import com.tom.fabriclibs.Events;
import com.tom.fabriclibs.client.GuiRegistry;
import com.tom.fabriclibs.client.RenderTypeLookup;
import com.tom.fabriclibs.events.client.ClientSetupLastEvent;
import com.tom.fabriclibs.events.client.ModelBakeEvent;
import com.tom.fabriclibs.events.client.RenderWorldLastEvent;
import com.tom.fabriclibs.ext.IRegistered;
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
		GuiRegistry.registerGui(StorageMod.storageTerminal, GuiStorageTerminal::new);
		GuiRegistry.registerGui(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);
		Events.INIT_BUS.addListener(ModelBakeEvent.class, ClientProxy::bakeModels);
		Events.INIT_BUS.addListener(ClientSetupLastEvent.class, e -> {
			BlockColors colors = MinecraftClient.getInstance().getBlockColors();
			colors.registerColorProvider((state, world, pos, tintIndex) -> {
				if (world != null) {
					try {
						BlockState mimicBlock = ((TileEntityPainted)world.getBlockEntity(pos)).getPaintedBlockState();
						return colors.getColor(mimicBlock, world, pos, tintIndex);
					} catch (Exception var8) {
						return -1;
					}
				}
				return -1;
			}, StorageMod.paintedTrim, StorageMod.invProxy);//StorageMod.invCableFramed
		});
		RenderTypeLookup.setRenderLayer(StorageMod.paintedTrim, RenderLayer.getTranslucent());
		//RenderTypeLookup.setRenderLayer(StorageMod.invCableFramed, e -> true);
		RenderTypeLookup.setRenderLayer(StorageMod.invProxy, RenderLayer.getTranslucent());
		Events.EVENT_BUS.addListener(RenderWorldLastEvent.class, ClientProxy::renderWorldLastEvent);
	}

	private static void bakeModels(ModelBakeEvent event) {
		bindPaintedModel(event, StorageMod.paintedTrim);
		//bindPaintedModel(event, StorageMod.invCableFramed);
		bindPaintedModel(event, StorageMod.invProxy);
	}

	private static void bindPaintedModel(ModelBakeEvent event, Block blockFor) {
		Identifier baseLoc = ((IRegistered) blockFor).getRegistryName();
		blockFor.getStateManager().getStates().forEach(st -> {
			ModelIdentifier resLoc = BlockModels.getModelId(baseLoc, st);
			event.getModelRegistry().put(resLoc, new BakedPaintedModel(blockFor, event.getModelRegistry().get(resLoc)));
		});
	}

	private static void renderWorldLastEvent(RenderWorldLastEvent evt) {
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if( player == null )
			return;

		if(!ItemWirelessTerminal.isPlayerHolding(player))
			return;

		BlockHitResult lookingAt = (BlockHitResult) player.rayTrace(StorageMod.CONFIG.wirelessRange, 0f, true);
		BlockState state = mc.world.getBlockState(lookingAt.getBlockPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			BlockPos pos = lookingAt.getBlockPos();
			Vec3d renderPos = mc.gameRenderer.getCamera().getPos();
			Tessellator.getInstance().getBuffer().begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
			MatrixStack ms = evt.getMatrixStack();
			ms.translate(pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z);
			float scale = 1.01f;
			ms.scale(scale, scale, scale);
			ms.translate(-0.001f, -0.001f, -0.001f);
			drawShape(ms, Tessellator.getInstance().getBuffer(), state.getOutlineShape(player.world, pos), 0, 0, 0, 1, 1, 1, 1);
			Tessellator.getInstance().draw();
		}
	}

	private static void drawShape(MatrixStack matrixStackIn, VertexConsumer bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.peek().getModel();
		shapeIn.forEachEdge((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
			bufferIn.vertex(matrix4f, (float)(p_230013_12_ + xIn), (float)(p_230013_14_ + yIn), (float)(p_230013_16_ + zIn)).color(red, green, blue, alpha).next();
			bufferIn.vertex(matrix4f, (float)(p_230013_18_ + xIn), (float)(p_230013_20_ + yIn), (float)(p_230013_22_ + zIn)).color(red, green, blue, alpha).next();
		});
	}

	public static void tooltip(String key, List<Text> tooltip) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.translate("tooltip.toms_storage." + key).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(new LiteralText(sp[i]));
			}
		} else {
			tooltip.add(new TranslatableText("tooltip.toms_storage.hold_shift_for_info").formatted(Formatting.ITALIC, Formatting.GRAY));
		}
	}
}
