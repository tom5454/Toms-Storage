package com.tom.storagemod.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.tom.storagemod.block.entity.CraftingTerminalBlockEntity;
import com.tom.storagemod.inventory.StoredItemStack;

class TerminalRecipePlacer extends ServerPlaceRecipe<CraftingInput, CraftingRecipe> {
	// Slot 0 is the crafting result; the grid occupies the slots right after it.
	private static final int GRID_SLOT_START = 1;

	private CraftingTerminalBlockEntity te;
	private Player player;

	public TerminalRecipePlacer(CraftingTerminalMenu p_135431_, CraftingTerminalBlockEntity te, Player player) {
		super(p_135431_);
		this.te = te;
		this.player = player;
	}

	@Override
	public void recipeClicked(ServerPlayer player, RecipeHolder<CraftingRecipe> recipe, boolean placeAll) {
		// Skips vanilla's testClearGrid() gate: it checks the player's real inventory, but
		// clearGrid() below returns items to the storage network instead.
		if (recipe != null && player.getRecipeBook().contains(recipe)) {
			this.inventory = player.getInventory();
			this.stackedContents.clear();
			player.getInventory().fillStackedContents(this.stackedContents);
			this.menu.fillCraftSlotsStackedContents(this.stackedContents);
			if (this.stackedContents.canCraft(recipe.value(), null)) {
				this.handleRecipeClicked(recipe, placeAll);
			} else {
				this.clearGrid();
				player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
			}

			player.getInventory().setChanged();
		}
	}

	@Override
	protected void handleRecipeClicked(RecipeHolder<CraftingRecipe> recipe, boolean placeAll) {
		// Roll back a partial/failed placement instead of leaving broken contents in the grid.
		// Only clears if something actually changed, so an early return in the vanilla method
		// (which can leave an unrelated, pre-existing grid untouched) doesn't wipe it.
		int gridSize = this.menu.getGridWidth() * this.menu.getGridHeight();
		int gridSlotEnd = GRID_SLOT_START + gridSize - 1;
		List<ItemStack> before = new ArrayList<>(gridSize);
		for (int i = GRID_SLOT_START; i <= gridSlotEnd; i++) {
			before.add(this.menu.getSlot(i).getItem().copy());
		}

		super.handleRecipeClicked(recipe, placeAll);

		if (!this.menu.recipeMatches(recipe)) {
			boolean changed = false;
			for (int i = GRID_SLOT_START; i <= gridSlotEnd; i++) {
				if (!ItemStack.matches(before.get(i - GRID_SLOT_START), this.menu.getSlot(i).getItem())) {
					changed = true;
					break;
				}
			}
			if (changed) {
				this.clearGrid();
			}
		}
	}

	@Override
	protected int moveItemToGrid(Slot slotToFill, ItemStack ingredientIn, int count) {
		int inInv = this.inventory.findSlotMatchingUnusedItem(ingredientIn);
		if (inInv != -1) {
			final ItemStack itemStack2 = this.inventory.getItem(inInv);
			int k;
			if (count < itemStack2.getCount()) {
				this.inventory.removeItem(inInv, count);
				k = count;
			} else {
				this.inventory.removeItemNoUpdate(inInv);
				k = itemStack2.getCount();
			}
			if (slotToFill.getItem().isEmpty()) {
				slotToFill.set(itemStack2.copyWithCount(k));
			} else {
				slotToFill.getItem().grow(k);
			}
			return count - k;
		} else if(this.te != null) {
			StoredItemStack st = this.te.pullStack(new StoredItemStack(ingredientIn), 1);
			if(st != null) {
				if (slotToFill.getItem().isEmpty()) {
					slotToFill.set(st.getActualStack());
				} else {
					slotToFill.getItem().grow(1);
				}
				return count - 1;
			}
		}
		return -1;
	}

	@Override
	protected void clearGrid() {
		this.te.clear(player);
		this.menu.clearCraftingContent();
	}
}