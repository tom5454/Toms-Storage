package com.tom.storagemod.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
			this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
			st.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
			drawIcon(st, mouseX, mouseY, pt);
		}
	}

	protected void drawIcon(GuiGraphics st, int mouseX, int mouseY, float pt) {
		st.blitSprite(RenderPipelines.GUI_TEXTURED, getIcon(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	public ResourceLocation getIcon() {
		return icon;
	}
}
