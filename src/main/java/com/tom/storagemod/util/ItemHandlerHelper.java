package com.tom.storagemod.util;

import net.minecraft.item.ItemStack;

public class ItemHandlerHelper {

	public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
		if (a.isEmpty() || !a.isItemEqual(b) || a.hasNbt() != b.hasNbt()) {
			return false;
		}
		return ((!a.hasNbt() || a.getNbt().equals(b.getNbt())));
	}

	public static ItemStack copyStackWithSize(ItemStack itemStack, int size) {
		if (size == 0)
			return ItemStack.EMPTY;
		ItemStack copy = itemStack.copy();
		copy.setCount(size);
		return copy;
	}

	public static boolean canItemStacksStackRelaxed(ItemStack a, ItemStack b)
	{
		if (a.isEmpty() || b.isEmpty() || a.getItem() != b.getItem())
			return false;

		if (!a.isStackable())
			return false;

		// Metadata value only matters when the item has subtypes
		// Vanilla stacks non-subtype items with different metadata together
		// TODO Item subtypes, is this still necessary?
		/* e.g. a stick with metadata 0 and a stick with metadata 1 stack
        if (a.getHasSubtypes() && a.getMetadata() != b.getMetadata())
            return false;
		 */
		if (a.hasNbt() != b.hasNbt())
			return false;

		return (!a.hasNbt() || a.getNbt().equals(b.getNbt()));
	}

	public static ItemStack insertItemStacked(IItemHandler inventory, ItemStack stack, boolean simulate)
	{
		if (inventory == null || stack.isEmpty())
			return stack;

		// not stackable -> just insert into a new slot
		if (!stack.isStackable())
		{
			return insertItem(inventory, stack, simulate);
		}

		int sizeInventory = inventory.getSlots();

		// go through the inventory and try to fill up already existing items
		for (int i = 0; i < sizeInventory; i++)
		{
			ItemStack slot = inventory.getStackInSlot(i);
			if (canItemStacksStackRelaxed(slot, stack))
			{
				stack = inventory.insertItem(i, stack, simulate);

				if (stack.isEmpty())
				{
					break;
				}
			}
		}

		// insert remainder into empty slots
		if (!stack.isEmpty())
		{
			// find empty slot
			for (int i = 0; i < sizeInventory; i++)
			{
				if (inventory.getStackInSlot(i).isEmpty())
				{
					stack = inventory.insertItem(i, stack, simulate);
					if (stack.isEmpty())
					{
						break;
					}
				}
			}
		}

		return stack;
	}

	public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate)
	{
		if (dest == null || stack.isEmpty())
			return stack;

		for (int i = 0; i < dest.getSlots(); i++)
		{
			stack = dest.insertItem(i, stack, simulate);
			if (stack.isEmpty())
			{
				return ItemStack.EMPTY;
			}
		}

		return stack;
	}
}
