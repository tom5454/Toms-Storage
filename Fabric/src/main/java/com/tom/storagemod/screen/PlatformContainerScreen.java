package com.tom.storagemod.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public abstract class PlatformContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IScreen {

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
}
