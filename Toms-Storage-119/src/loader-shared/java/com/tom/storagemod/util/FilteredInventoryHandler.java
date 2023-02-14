package com.tom.storagemod.util;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

public class FilteredInventoryHandler implements IItemHandler {
	private IItemHandler parent;
	private ItemPredicate filter;
	private boolean keepLastInSlot;

	public FilteredInventoryHandler(IItemHandler parent, ItemPredicate filter, boolean keepLastInSlot) {
		this.parent = parent;
		this.filter = filter;
		this.keepLastInSlot = keepLastInSlot;
	}

	@Override
	public int getSlots() {
		return parent.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int paramInt) {
		ItemStack inSlot = parent.getStackInSlot(paramInt);
		if(!isInFilter(inSlot))return ItemStack.EMPTY;
		if(keepLastInSlot) {
			ItemStack s = inSlot.copy();
			s.shrink(1);
			return s;
		}
		return inSlot;
	}

	@Override
	public ItemStack insertItem(int paramInt, ItemStack stack, boolean paramBoolean) {
		if(!isInFilter(stack))return stack;
		return parent.insertItem(paramInt, stack, paramBoolean);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean paramBoolean) {
		ItemStack inSlot = parent.getStackInSlot(slot);
		if(keepLastInSlot) {
			amount = Math.min(inSlot.getCount() - 1, amount);
			if(amount < 1)return ItemStack.EMPTY;
		}
		if(!isInFilter(inSlot))return ItemStack.EMPTY;
		return parent.extractItem(slot, amount, paramBoolean);
	}

	@Override
	public int getSlotLimit(int paramInt) {
		return parent.getSlotLimit(paramInt);
	}

	@Override
	public boolean isItemValid(int paramInt, ItemStack paramItemStack) {
		return isInFilter(paramItemStack);
	}

	private boolean isInFilter(ItemStack inSlot) {
		return filter.test(inSlot);
	}
}
