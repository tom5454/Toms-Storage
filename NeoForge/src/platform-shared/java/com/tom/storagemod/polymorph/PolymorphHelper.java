package com.tom.storagemod.polymorph;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.illusivesoulworks.polymorph.api.PolymorphApi;

public class PolymorphHelper {

	public static Optional<RecipeHolder<CraftingRecipe>> getRecipe(Player pl, RecipeType<CraftingRecipe> crafting, CraftingInput input, Level level) {
		return PolymorphApi.getInstance().getRecipeManager().getPlayerRecipe(pl.containerMenu, crafting, input, level, pl);
	}

}
