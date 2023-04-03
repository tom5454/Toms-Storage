package com.tom.storagemod.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiStorageTerminal extends GuiStorageTerminalBase<ContainerStorageTerminal> {
	private static final Identifier gui = new Identifier("toms_storage", "textures/gui/storage_terminal.png");

	public GuiStorageTerminal(ContainerStorageTerminal screenContainer, PlayerInventory inv, Text titleIn) {
		super(screenContainer, inv, titleIn, 5, 202, 7, 17);
	}

	@Override
	protected void init() {
		backgroundWidth = 194;
		backgroundHeight = 202;
		super.init();
	}

	@Override
	public Identifier getGui() {
		return gui;
	}

	@Override
	public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		super.render(st, mouseX, mouseY, partialTicks);
	}
}
