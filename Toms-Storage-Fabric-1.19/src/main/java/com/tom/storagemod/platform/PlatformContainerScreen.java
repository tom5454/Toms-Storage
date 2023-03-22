package com.tom.storagemod.platform;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class PlatformContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	public PlatformContainerScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	public int getGuiLeft() {
		return leftPos;
	}

	public int getGuiTop() {
		return topPos;
	}

	public Slot getSlotUnderMouse() {
		return null;
	}

	@Override
	public void setBlitOffset(int p_93251_) {
		super.setBlitOffset(p_93251_);
		this.itemRenderer.blitOffset = p_93251_;
	}

	public void renderItem(PoseStack ps, ItemStack stack, int x, int y) {
		this.itemRenderer.renderAndDecorateItem(this.minecraft.player, stack, x, y, 0);
	}

	public void renderItemDecorations(PoseStack ps, ItemStack stack, int x, int y) {
		this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y, null);
	}
}
