package com.tom.storagemod.gui;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.systems.RenderSystem;

public class FilteredScreen extends HandledScreen<FilteredMenu> {
	private static final Identifier DISPENSER_GUI_TEXTURES = new Identifier("textures/gui/container/dispenser.png");

	public FilteredScreen(FilteredMenu container, PlayerInventory playerInventory, Text textComponent) {
		super(container, playerInventory, textComponent);
	}

	@Override
	protected void init() {
		super.init();
		//this.titleX = (this.x - this.textRenderer.getWidth(this.title)) / 2;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, DISPENSER_GUI_TEXTURES);
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrixStack, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}
}
