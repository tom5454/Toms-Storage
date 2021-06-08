package com.tom.storagemod.util;

import java.util.Set;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FilteredInventoryWrapper extends InventoryWrapper {
	private Inventory filter;
	private InventoryWrapper inv;
	public FilteredInventoryWrapper(InventoryWrapper inv, Inventory filter) {
		super(null, null);
		this.inv = inv;
		this.filter = filter;
	}

	private boolean isInFilter(ItemStack stack) {
		for(int i = 0;i<filter.size();i++) {
			ItemStack is = filter.getStack(i);
			if(is.isEmpty())continue;
			if(ItemStack.areItemsEqual(stack, is))return true;
		}
		return false;
	}

	@Override
	public IItemHandler wrap() {
		return new FilteredInventoryHandler(inv.wrap(), filter);
	}

	@Override
	public int size() {
		return inv.size();
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}

	@Override
	public ItemStack getStack(int paramInt) {
		ItemStack is = inv.getStack(paramInt);
		if(isInFilter(is))return is;
		else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int paramInt1, int paramInt2) {
		ItemStack is = inv.getStack(paramInt1);
		if(isInFilter(is))return inv.removeStack(paramInt1, paramInt2);
		else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int paramInt) {
		ItemStack is = inv.getStack(paramInt);
		if(isInFilter(is))return inv.removeStack(paramInt);
		else return ItemStack.EMPTY;
	}

	@Override
	public void setStack(int paramInt, ItemStack paramItemStack) {
		inv.setStack(paramInt, paramItemStack);
	}

	@Override
	public int getMaxCountPerStack() {
		return inv.getMaxCountPerStack();
	}

	@Override
	public void markDirty() {
		inv.markDirty();
	}

	@Override
	public boolean isValid(int slot, ItemStack stack, Boolean extract) {
		if(!isInFilter(stack))return false;
		ItemStack is = inv.getStack(slot);
		if(!is.isEmpty() && !isInFilter(is))return false;
		return true;
	}

	@Override
	public int count(Item item) {
		return inv.count(item);
	}

	@Override
	public boolean containsAny(Set<Item> items) {
		return inv.containsAny(items);
	}

	@Override
	public Inventory getInventory() {
		return inv.getInventory();
	}

	@Override
	public boolean canAccess(int slot) {
		return inv.canAccess(slot);
	}

	@Override
	public String toString() {
		return inv.toString();
	}
}
