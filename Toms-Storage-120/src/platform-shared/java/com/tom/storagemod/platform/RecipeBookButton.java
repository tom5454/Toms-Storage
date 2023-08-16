package com.tom.storagemod.platform;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class RecipeBookButton extends ImageButton {
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

	public RecipeBookButton(int x, int y, OnPress onPress) {
		super(x, y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, onPress);
	}
}
