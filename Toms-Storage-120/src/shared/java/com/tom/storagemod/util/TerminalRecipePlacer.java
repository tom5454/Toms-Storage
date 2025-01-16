package com.tom.storagemod.util;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.tile.CraftingTerminalBlockEntity;

public class TerminalRecipePlacer extends ServerPlaceRecipe<CraftingContainer> {
	private CraftingTerminalBlockEntity te;
	private Player player;

	public TerminalRecipePlacer(RecipeBookMenu<CraftingContainer> p_135431_, CraftingTerminalBlockEntity te, Player player) {
		super(p_135431_);
		this.te = te;
		this.player = player;
	}

	@Override
	protected void moveItemToGrid(Slot slotToFill, ItemStack ingredientIn) {
		int i = this.inventory.findSlotMatchingUnusedItem(ingredientIn);
		if (i != -1) {
			ItemStack itemstack = this.inventory.getItem(i).copy();
			if (!itemstack.isEmpty()) {
				if (itemstack.getCount() > 1) {
					this.inventory.removeItem(i, 1);
				} else {
					this.inventory.removeItemNoUpdate(i);
				}

				itemstack.setCount(1);
				if (slotToFill.getItem().isEmpty()) {
					slotToFill.set(itemstack);
				} else {
					slotToFill.getItem().grow(1);
				}

			}
		} else if(te != null) {
			StoredItemStack st = te.pullStackFuzzy(new StoredItemStack(ingredientIn), 1);
			if(st != null) {
				if (slotToFill.getItem().isEmpty()) {
					slotToFill.set(st.getActualStack());
				} else {
					slotToFill.getItem().grow(1);
				}
			}
		}
	}

	@Override
	protected void clearGrid() {
		te.clear(player);
		this.menu.clearCraftingContent();
	}
}
