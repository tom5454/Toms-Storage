package com.tom.storagemod.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.menu.StorageTerminalMenu;

public class StorageTerminalScreen extends AbstractStorageTerminalScreen<StorageTerminalMenu> {
	private static final ResourceLocation gui = ResourceLocation.tryBuild("toms_storage", "textures/gui/storage_terminal.png");

	public StorageTerminalScreen(StorageTerminalMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn, 5, 202, 7, 17);
	}

	@Override
	protected void init() {
		imageWidth = 194;
		imageHeight = 202;
		super.init();
		onPacket();
	}

	@Override
	public ResourceLocation getGui() {
		return gui;
	}
}
