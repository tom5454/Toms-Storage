package com.tom.storagemod.screen.widget;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;

import com.tom.storagemod.menu.CraftingTerminalMenu;

public class CraftingTerminalRecipeBookWidget extends RecipeBookComponent<CraftingTerminalMenu> {
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
			Identifier.withDefaultNamespace("recipe_book/filter_enabled"),
			Identifier.withDefaultNamespace("recipe_book/filter_disabled"),
			Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
			Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
			);
	private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
			new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING),
			new RecipeBookComponent.TabInfo(Items.IRON_AXE, Items.GOLDEN_SWORD, RecipeBookCategories.CRAFTING_EQUIPMENT),
			new RecipeBookComponent.TabInfo(Items.BRICKS, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS),
			new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.APPLE, RecipeBookCategories.CRAFTING_MISC),
			new RecipeBookComponent.TabInfo(Items.REDSTONE, RecipeBookCategories.CRAFTING_REDSTONE)
			);

	private boolean canDisplay(RecipeDisplay recipeDisplay) {
		int i = 3;
		int j = 3;
		Objects.requireNonNull(recipeDisplay);

		return switch (recipeDisplay) {
		case ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay -> i >= shapedCraftingRecipeDisplay.width() && j >= shapedCraftingRecipeDisplay.height();
		case ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay -> i * j >= shapelessCraftingRecipeDisplay.ingredients().size();
		default -> false;
		};
	}

	public CraftingTerminalRecipeBookWidget(CraftingTerminalMenu recipeBookMenu) {
		super(recipeBookMenu, TABS);
	}

	@Override
	protected WidgetSprites getFilterButtonTextures() {
		return FILTER_BUTTON_SPRITES;
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return slot.index < 10;
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
		recipeCollection.selectRecipes(stackedItemContents, this::canDisplay);
	}

	@Override
	protected Component getRecipeFilterName() {
		return ONLY_CRAFTABLES_TOOLTIP;
	}

	@Override
	protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
		ghostSlots.setResult(this.menu.getCraftingResultSlot(), contextMap, recipeDisplay.result());
		Objects.requireNonNull(recipeDisplay);
		switch (recipeDisplay) {
		case ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay:
			List<Slot> list = this.menu.getInputGridSlots();
			PlaceRecipeHelper.placeRecipe(
					3,
					3,
					shapedCraftingRecipeDisplay.width(),
					shapedCraftingRecipeDisplay.height(),
					shapedCraftingRecipeDisplay.ingredients(),
					(slotDisplay, ix, jx, k) -> {
						Slot slot = list.get(ix);
						ghostSlots.setInput(slot, contextMap, slotDisplay);
					}
					);
			break;
		case ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay:
			label15: {
				List<Slot> list2 = this.menu.getInputGridSlots();
				int i = Math.min(shapelessCraftingRecipeDisplay.ingredients().size(), list2.size());

				for (int j = 0; j < i; j++) {
					ghostSlots.setInput(list2.get(j), contextMap, shapelessCraftingRecipeDisplay.ingredients().get(j));
				}
				break label15;
			}
		default:
			break;
		}
	}

}
