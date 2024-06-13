package com.tom.storagemod.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.StorageMod;

public class IconButton extends Button {
	protected static final WidgetSprites SPRITES = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/slot_button"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/slot_button_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/slot_button_hovered")
			);

	protected Component name;
	protected ResourceLocation icon;

	public IconButton(int x, int y, Component name, ResourceLocation icon, OnPress pressable) {
		super(x, y, 16, 16, Component.empty(), pressable, Button.DEFAULT_NARRATION);
		this.name = name;
		this.icon = icon;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
		if (this.visible) {
			int x = getX();
			int y = getY();
			st.setColor(1.0f, 1.0f, 1.0f, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
			st.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
			drawIcon(st, mouseX, mouseY, pt);
			st.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	protected void drawIcon(GuiGraphics st, int mouseX, int mouseY, float pt) {
		st.blitSprite(getIcon(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	public ResourceLocation getIcon() {
		return icon;
	}
}
