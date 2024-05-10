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

	@Override
	public boolean mouseScrolled(double x, double y, double xd, double yd) {
		return mouseScrolled(x, y, yd) || super.mouseScrolled(x, y, xd, yd);
	}

	public boolean mouseScrolled(double x, double y, double delta) {
		return false;
	}

	private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/creative_inventory/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation("container/creative_inventory/scroller_disabled");
	public void drawScroll(GuiGraphics gr, int x, int y, boolean en) {
		gr.blitSprite(en ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE, x, y, 12, 15);
	}
}
