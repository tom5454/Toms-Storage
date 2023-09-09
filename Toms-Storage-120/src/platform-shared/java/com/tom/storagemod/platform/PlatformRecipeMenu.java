package com.tom.storagemod.platform;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;

public abstract class PlatformRecipeMenu extends RecipeBookMenu<CraftingContainer> {

	public PlatformRecipeMenu(MenuType<?> menuType, int i) {
		super(menuType, i);
	}

	public ServerPlaceRecipe<CraftingContainer> getRecipePlacer() {
		return new ServerPlaceRecipe<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handlePlacement(boolean bl, Recipe<?> recipeHolder, ServerPlayer serverPlayer) {
		ServerPlaceRecipe f = getRecipePlacer();
		if (f != null)f.recipeClicked(serverPlayer, recipeHolder, bl);
	}
}
