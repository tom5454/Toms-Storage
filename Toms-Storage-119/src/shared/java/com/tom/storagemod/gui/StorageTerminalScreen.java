package com.tom.storagemod.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.vertex.PoseStack;

public class StorageTerminalScreen extends AbstractStorageTerminalScreen<StorageTerminalMenu> {
	private static final ResourceLocation gui = new ResourceLocation("toms_storage", "textures/gui/storage_terminal.png");

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

	@Override
	public void render(PoseStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		super.render(st, mouseX, mouseY, partialTicks);
	}
}
