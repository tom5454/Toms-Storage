package com.tom.storagemod.platform;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

public class RecipeBookButton extends ImageButton {

	public RecipeBookButton(int x, int y, OnPress onPress) {
		super(x, y, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, onPress);
	}
}
