package com.tom.storagemod.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.tom.storagemod.block.entity.CraftingTerminalBlockEntity;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.util.TerminalSyncManager;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class TerminalCraftingFiller {
	private CraftingTerminalBlockEntity te;
	private Player player;
	private Int2ObjectMap<List<ItemStack>> allItems = new Int2ObjectOpenHashMap<>();
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
		int rw;
		if (recipe instanceof ShapedRecipe sr) {
			rw = sr.getWidth();
		} else {
			int cnt = recipe.getIngredients().size();
			if (cnt == 1) {
				rw = 1;
			} else if (cnt <= 4) {
				rw = 2;
			} else {
				rw = 3;
			}
		}
		var ings = recipe.getIngredients();
		for (int i = 0; i < ings.size(); i++) {
			Ingredient ingr = ings.get(i);
			if (ingr.isEmpty())continue;
			int x = i % rw;
			int y = i / rw;
			boolean filled = false;
			for (int v : ingr.getStackingIds()) {
				var lst = allItems.get(v);
				if (lst != null) {
					for (var item : lst) {
						if (ingr.test(item)) {
							var pull = te.pullStack(new StoredItemStack(item), 1);
							if (pull != null) {
								filled = true;
								te.setCraftSlot(x, y, pull.getActualStack());
								break;
							} else {
								int id = player.getInventory().findSlotMatchingItem(item);
								if (id != -1) {
									te.setCraftSlot(x, y, player.getInventory().removeItem(id, 1));
									filled = true;
									break;
								}
							}
						}
					}
					if (filled)break;
				}
			}
		}
	}

	public void accountStack(ItemStack st) {
		if (st.has(DataComponents.CUSTOM_NAME))return;
		int index = StackedContents.getStackingIndex(st);
		allItems.computeIfAbsent(index, __ -> new ArrayList<>()).add(st);
	}
}
