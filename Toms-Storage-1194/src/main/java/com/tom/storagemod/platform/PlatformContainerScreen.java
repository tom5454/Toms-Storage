package com.tom.storagemod.platform;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class PlatformContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
	private int blitOffset;

	public PlatformContainerScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	public void setBlitOffset(int bo) {
		this.blitOffset = bo;
	}

	public int getBlitOffset() {
		return this.blitOffset;
	}

	public void renderItem(PoseStack ps, ItemStack stack, int x, int y) {
		this.itemRenderer.renderAndDecorateItem(ps, stack, x, y, 0, blitOffset);
	}

	public void renderItemDecorations(PoseStack ps, ItemStack stack, int x, int y) {
		this.itemRenderer.renderGuiItemDecorations(ps, this.font, stack, x, y, null);
	}
}
