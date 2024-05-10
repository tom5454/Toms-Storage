package com.tom.storagemod.gui;

import java.util.function.Supplier;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonExt extends Button {

	public ButtonExt(int x, int y, int w, int h, Component text, OnPress onPress) {
		super(x, y, w, h, text, onPress, Supplier::get);
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
}
