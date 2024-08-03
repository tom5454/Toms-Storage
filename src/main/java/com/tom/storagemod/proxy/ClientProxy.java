package com.tom.storagemod.proxy;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
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
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.gui.GuiCraftingTerminal;
import com.tom.storagemod.gui.GuiFiltered;
import com.tom.storagemod.gui.GuiLevelEmitter;
import com.tom.storagemod.gui.GuiStorageTerminal;
import com.tom.storagemod.item.ItemWirelessTerminal;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.TileEntityPainted;

public class ClientProxy implements IProxy {

	@Override
	public void setup() {

	}

	public static KeyBinding openTerm;

	@Override
	public void clientSetup() {
		ScreenManager.register(StorageMod.storageTerminal, GuiStorageTerminal::new);
		ScreenManager.register(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);
		ScreenManager.register(StorageMod.filteredConatiner, GuiFiltered::new);
		ScreenManager.register(StorageMod.levelEmitterConatiner, GuiLevelEmitter::new);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::bakeModels);
		RenderTypeLookup.setRenderLayer(StorageMod.paintedTrim, e -> true);
		RenderTypeLookup.setRenderLayer(StorageMod.invCableFramed, e -> true);
		RenderTypeLookup.setRenderLayer(StorageMod.invProxy, e -> true);
		RenderTypeLookup.setRenderLayer(StorageMod.levelEmitter, RenderType.cutout());
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
		openTerm = new KeyBinding("key.toms_storage.open_terminal", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrCreate(66), "key.categories.gameplay");
		ClientRegistry.registerKeyBinding(openTerm);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::renderWorldLastEvent);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::clientTick);
	}

	private static void bakeModels(ModelBakeEvent event) {
		bindPaintedModel(event, StorageMod.paintedTrim);
		bindPaintedModel(event, StorageMod.invCableFramed);
		bindPaintedModel(event, StorageMod.invProxy);
	}

	private static void bindPaintedModel(ModelBakeEvent event, Block blockFor) {
		ResourceLocation baseLoc = blockFor.delegate.name();
		blockFor.getStateDefinition().getPossibleStates().forEach(st -> {
			ModelResourceLocation resLoc = BlockModelShapes.stateToModelLocation(baseLoc, st);
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
		BlockState state = mc.level.getBlockState(lookingAt.getBlockPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			BlockPos pos = lookingAt.getBlockPos();
			Vector3d renderPos = mc.gameRenderer.getMainCamera().getPosition();
			Tessellator.getInstance().getBuilder().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			MatrixStack ms = evt.getMatrixStack();
			ms.translate(pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z);
			float scale = 1.01f;
			ms.scale(scale, scale, scale);
			ms.translate(-0.001f, -0.001f, -0.001f);
			drawShape(ms, Tessellator.getInstance().getBuilder(), state.getOcclusionShape(player.level, pos), 0, 0, 0, 1, 1, 1, 1);
			Tessellator.getInstance().end();
		}
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.last().pose();
		shapeIn.forAllEdges((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
			bufferIn.vertex(matrix4f, (float)(p_230013_12_ + xIn), (float)(p_230013_14_ + yIn), (float)(p_230013_16_ + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.vertex(matrix4f, (float)(p_230013_18_ + xIn), (float)(p_230013_20_ + yIn), (float)(p_230013_22_ + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

	public static void tooltip(String key, List<ITextComponent> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean addShift, List<ITextComponent> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.get("tooltip.toms_storage." + key, args).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(new StringTextComponent(sp[i]));
			}
		} else if(addShift) {
			tooltip.add(new TranslationTextComponent("tooltip.toms_storage.hold_shift_for_info").withStyle(TextFormatting.ITALIC, TextFormatting.GRAY));
		}
	}

	public static void clientTick(ClientTickEvent evt) {
		if (Minecraft.getInstance().player == null || evt.phase == Phase.START)
			return;

		if(openTerm.consumeClick()) {
			NetworkHandler.openTerminal();
		}
	}
}
