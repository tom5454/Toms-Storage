package com.tom.storagemod.gui;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.RecipeBookMenu;

public class PlatformServerPlaceRecipe<C extends Container> extends ServerPlaceRecipe<C> {

	public PlatformServerPlaceRecipe(RecipeBookMenu<C> recipeBookMenu) {
		super(recipeBookMenu);
	}

	@Override
	protected void clearGrid() {
		clearGrid(false);
	}

	protected void clearGrid(boolean b) {
		super.clearGrid();
	}
}
