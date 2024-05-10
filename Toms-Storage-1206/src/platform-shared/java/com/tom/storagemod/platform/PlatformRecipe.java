package com.tom.storagemod.platform;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public record PlatformRecipe(RecipeHolder<?> recipe) {

	@SuppressWarnings("unchecked")
	public Recipe<Container> cast() {
		return (Recipe<Container>) recipe.value();
	}

	public NonNullList<ItemStack> getRemainingItems(Container craftMatrix) {
		return cast().getRemainingItems(craftMatrix);
	}

	public boolean matches(Container craftMatrix, Level level) {
		return cast().matches(craftMatrix, level);
	}

	public static PlatformRecipe of(RecipeHolder<CraftingRecipe> v) {
		return v != null ? new PlatformRecipe(v) : null;
	}

	public ItemStack assemble(Container craftMatrix, RegistryAccess registryAccess) {
		return cast().assemble(craftMatrix, registryAccess);
	}

}
