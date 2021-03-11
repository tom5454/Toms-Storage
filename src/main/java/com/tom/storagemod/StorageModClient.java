package com.tom.storagemod;

import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.gui.GuiCraftingTerminal;
import com.tom.storagemod.gui.GuiFiltered;
import com.tom.storagemod.gui.GuiLevelEmitter;
import com.tom.storagemod.gui.GuiStorageTerminal;
import com.tom.storagemod.model.BakedPaintedModel;
import com.tom.storagemod.tile.TileEntityPainted;

public class StorageModClient implements ClientModInitializer {
	protected static final Identifier PAINT = new Identifier(StorageMod.modid, "paint");

	@Override
	public void onInitializeClient() {
		ScreenRegistry.register(StorageMod.storageTerminal, GuiStorageTerminal::new);
		ScreenRegistry.register(StorageMod.craftingTerminalCont, GuiCraftingTerminal::new);
		ScreenRegistry.register(StorageMod.filteredConatiner, GuiFiltered::new);
		ScreenRegistry.register(StorageMod.levelEmitterConatiner, GuiLevelEmitter::new);

		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.paintedTrim, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCableFramed, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.invCablePainted, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(StorageMod.levelEmitter, RenderLayer.getCutout());

		ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_S2C, (mc, h, buf, rp) -> {
			CompoundTag tag = buf.readUnlimitedCompoundTag();
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
					BlockState mimicBlock = ((TileEntityPainted)world.getBlockEntity(pos)).getPaintedBlockState();
					return MinecraftClient.getInstance().getBlockColors().getColor(mimicBlock, world, pos, tintIndex);
				} catch (Exception var8) {
					return -1;
				}
			}
			return -1;
		}, StorageMod.paintedTrim, StorageMod.invCablePainted);
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
