package com.tom.storagemod.tile;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class FilteredInventoryHandler implements IItemHandler {
	private IItemHandler parent;
	private Inventory filter;
	public FilteredInventoryHandler(IItemHandler parent, Inventory filter) {
		this.parent = parent;
		this.filter = filter;
	}

	@Override
	public int getSlots() {
		return parent.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int paramInt) {
		ItemStack inSlot = parent.getStackInSlot(paramInt);
		if(!isInFilter(inSlot))return ItemStack.EMPTY;
		return inSlot;
	}

	@Override
	public ItemStack insertItem(int paramInt, ItemStack stack, boolean paramBoolean) {
		if(!isInFilter(stack))return stack;
		return parent.insertItem(paramInt, stack, paramBoolean);
	}

	@Override
	public ItemStack extractItem(int slot, int paramInt2, boolean paramBoolean) {
		ItemStack inSlot = parent.getStackInSlot(slot);
		if(!isInFilter(inSlot))return ItemStack.EMPTY;
		return parent.extractItem(slot, paramInt2, paramBoolean);
	}

	@Override
	public int getSlotLimit(int paramInt) {
		return parent.getSlotLimit(paramInt);
	}

	@Override
	public boolean isItemValid(int paramInt, ItemStack paramItemStack) {
		return isInFilter(paramItemStack);
	}

	private boolean isInFilter(ItemStack stack) {
		for(int i = 0;i<filter.size();i++) {
			ItemStack is = filter.getStack(i);
			if(is.isEmpty())continue;
			if(ItemStack.areItemsEqual(stack, is))return true;
		}
		return false;
	}
}
