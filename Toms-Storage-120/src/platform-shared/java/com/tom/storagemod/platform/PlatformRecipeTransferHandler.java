package com.tom.storagemod.platform;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

public interface PlatformRecipeTransferHandler<C extends AbstractContainerMenu> extends IRecipeTransferHandler<C, CraftingRecipe> {

	@Override
	public default RecipeType<CraftingRecipe> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}
}
