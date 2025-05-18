package com.tom.storagemod.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import com.tom.storagemod.block.entity.CraftingTerminalBlockEntity;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.util.TerminalSyncManager;

public class TerminalCraftingFiller {
	private CraftingTerminalBlockEntity te;
	private Player player;
	private Map<Holder<Item>, List<ItemStack>> allItems = new HashMap<>();
	private TerminalSyncManager sync;

	public TerminalCraftingFiller(CraftingTerminalBlockEntity te, Player player, TerminalSyncManager sync) {
		this.te = te;
		this.player = player;
		this.sync = sync;
	}

	public void placeRecipe(Recipe<?> recipe) {
		te.clear(player);
		sync.fillCraftingFiller(this);
		for (var i : player.getInventory().items) {
			accountStack(i);
		}
		var ings = recipe.placementInfo().ingredients();
		PlaceRecipeHelper.placeRecipe(3, 3, recipe, recipe.placementInfo().slotsToIngredientIndex(), (idx, slotId, _1, _2) -> {
			if (idx == -1)return;
			Ingredient ingr = ings.get(idx);
			boolean filled = false;
			for (Holder<Item> v : Platform.getIngredientItems(ingr)) {
				var lst = allItems.get(v);
				if (lst != null) {
					for (var item : lst) {
						if (ingr.test(item)) {
							var pull = te.pullStack(new StoredItemStack(item), 1);
							if (pull != null) {
								filled = true;
								te.setCraftSlot(slotId, pull.getActualStack());
								break;
							} else {
								int id = player.getInventory().findSlotMatchingItem(item);
								if (id != -1) {
									te.setCraftSlot(slotId, player.getInventory().removeItem(id, 1));
									filled = true;
									break;
								}
							}
						}
					}
					if (filled)break;
				}
			}
		});
	}

	public void accountStack(ItemStack st) {
		if (st.has(DataComponents.CUSTOM_NAME))return;
		allItems.computeIfAbsent(st.getItemHolder(), __ -> new ArrayList<>()).add(st);
	}
}
