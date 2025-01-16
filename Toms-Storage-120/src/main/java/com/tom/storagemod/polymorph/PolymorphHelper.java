package com.tom.storagemod.polymorph;

import java.util.Optional;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;

import com.tom.storagemod.gui.CraftingTerminalMenu;
import com.tom.storagemod.tile.CraftingTerminalBlockEntity;

public class PolymorphHelper {

	public static <C extends Container, T extends Recipe<C>> Optional<T> getRecipe(CraftingTerminalBlockEntity be, RecipeType<T> type, C inventory, Level world) {
		return RecipeSelection.getBlockEntityRecipe(type, inventory, world, be);
	}

	public static void init() {
		IPolymorphCommon commonApi = PolymorphApi.common();
		commonApi.registerBlockEntity2RecipeData(pTileEntity -> {
			if (pTileEntity instanceof CraftingTerminalBlockEntity) {
				return new CraftingTerminalRecipeData((CraftingTerminalBlockEntity) pTileEntity);
			}
			return null;
		});
		commonApi.registerContainer2BlockEntity(pContainer -> {
			if (pContainer instanceof CraftingTerminalMenu cnt) {
				return cnt.getTerminal();
			}
			return null;
		});
	}
}
