package com.tom.storagemod.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import com.mojang.blaze3d.matrix.MatrixStack;

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
	protected void func_230450_a_(MatrixStack st, float partialTicks, int mouseX, int mouseY) {
		mc.textureManager.bindTexture(gui);
		this.blit(st, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public ResourceLocation getGui() {
		return gui;
	}

	@Override
	public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		super.render(st, mouseX, mouseY, partialTicks);
	}
}
