package com.tom.storagemod.util;

import net.minecraft.item.ItemStack;

public class EmptyHandler implements IItemHandler {
	public static final IItemHandler INSTANCE = new EmptyHandler();

	@Override
	public int getSlots()
	{
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override

	public ItemStack insertItem(int slot,  ItemStack stack, boolean simulate)
	{
		return stack;
	}

	@Override

	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public boolean isItemValid(int slot,  ItemStack stack)
	{
		return false;
	}
}
