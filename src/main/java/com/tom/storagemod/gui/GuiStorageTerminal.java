package com.tom.storagemod.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiStorageTerminal extends GuiStorageTerminalBase<ContainerStorageTerminal> {
	private static final ResourceLocation gui = new ResourceLocation("toms_storage", "textures/gui/storage_terminal.png");

	public GuiStorageTerminal(ContainerStorageTerminal screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected void init() {
		xSize = 194;
		ySize = 202;
		rowCount = 5;
		super.init();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.textureManager.bindTexture(gui);
		this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	public ResourceLocation getGui() {
		return gui;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);
	}
}
