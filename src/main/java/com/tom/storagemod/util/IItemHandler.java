package com.tom.storagemod.util;

import net.minecraft.item.ItemStack;

public interface IItemHandler {
	int getSlots();

	ItemStack getStackInSlot(int paramInt);

	ItemStack insertItem(int paramInt, ItemStack paramItemStack, boolean paramBoolean);

	ItemStack extractItem(int paramInt1, int paramInt2, boolean paramBoolean);

	int getSlotLimit(int paramInt);

	boolean isItemValid(int paramInt, ItemStack paramItemStack);
}
