package com.tom.storagemod.gui;

import java.util.function.Supplier;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;

public class PlatformButton extends Button {

	public PlatformButton(int x, int y, int w, int h, Component text, OnPress onPress) {
		super(x, y, w, h, text, onPress, Supplier::get);
	}

	public void renderButton(PoseStack st, int mouseX, int mouseY, float pt) {
		super.renderWidget(st, mouseX, mouseY, pt);
	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		renderButton(poseStack, i, j, f);
	}

	public int getYImage(boolean hov) {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isHoveredOrFocused()) {
			i = 2;
		}
		return i;
	}

	@Override
	public boolean isHoveredOrFocused() {
		return isHovered;
	}
}
