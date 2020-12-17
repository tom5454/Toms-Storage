package com.tom.storagemod.tile;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

public class FilteredInventoryHandler implements IItemHandler {
	private IItemHandler parent;
	private IInventory filter;
	public FilteredInventoryHandler(IItemHandler parent, IInventory filter) {
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
		for(int i = 0;i<filter.getSizeInventory();i++) {
			ItemStack is = filter.getStackInSlot(i);
			if(is.isEmpty())continue;
			if(ItemStack.areItemsEqual(stack, is))return true;
		}
		return false;
	}
}
