package com.tom.storagemod.jei;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

public interface PlatformRecipeTransferHandler<C extends AbstractContainerMenu> extends IRecipeTransferHandler<C, RecipeHolder<CraftingRecipe>> {

	@Override
	public default RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	public @Nullable IRecipeTransferError transferRecipe(C container, CraftingRecipe recipe,
			IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer);

	@Override
	default @Nullable IRecipeTransferError transferRecipe(C container, RecipeHolder<CraftingRecipe> recipe,
			IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		return transferRecipe(container, recipe.value(), recipeSlots, player, maxTransfer, doTransfer);
	}
}
