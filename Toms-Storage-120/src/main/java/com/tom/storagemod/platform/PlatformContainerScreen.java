package com.tom.storagemod.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class PlatformContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	public PlatformContainerScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderBackground(guiGraphics);
	}

	protected static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	public void drawScroll(GuiGraphics gr, int x, int y, boolean en) {
		gr.blit(creativeInventoryTabs, x, y, 232 + (en ? 0 : 12), 0, 12, 15);
	}
}
