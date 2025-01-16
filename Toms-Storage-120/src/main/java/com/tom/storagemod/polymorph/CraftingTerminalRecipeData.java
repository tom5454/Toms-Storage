package com.tom.storagemod.polymorph;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;

import com.tom.storagemod.tile.CraftingTerminalBlockEntity;

public class CraftingTerminalRecipeData extends AbstractBlockEntityRecipeData<CraftingTerminalBlockEntity> {

	public CraftingTerminalRecipeData(CraftingTerminalBlockEntity owner) {
		super(owner);
	}

	@Override
	protected NonNullList<ItemStack> getInput() {
		CraftingContainer craftingInventory = this.getOwner().getCraftingInv();

		if (craftingInventory != null) {
			NonNullList<ItemStack> stacks =
					NonNullList.withSize(craftingInventory.getContainerSize(), ItemStack.EMPTY);

			for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
				stacks.set(i, craftingInventory.getItem(i));
			}
			return stacks;
		}
		return NonNullList.create();
	}

}
