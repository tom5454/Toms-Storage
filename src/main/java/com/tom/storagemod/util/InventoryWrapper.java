package com.tom.storagemod.util;

import java.util.Set;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Direction;

public class InventoryWrapper {
	public static final InventoryWrapper INSTANCE = null;
	private Inventory inv;
	private Direction dir;

	public InventoryWrapper(Inventory inv, Direction dir) {
		this.inv = inv;
		this.dir = dir;
	}

	public IItemHandler wrap() {
		return InvWrapper.wrap(inv, dir);
	}

	public int size() {
		return inv.size();
	}

	public boolean isEmpty() {
		return inv.isEmpty();
	}

	public ItemStack getStack(int paramInt) {
		return inv.getStack(paramInt);
	}

	public ItemStack removeStack(int paramInt1, int paramInt2) {
		return inv.removeStack(paramInt1, paramInt2);
	}

	public ItemStack removeStack(int paramInt) {
		return inv.removeStack(paramInt);
	}

	public void setStack(int paramInt, ItemStack paramItemStack) {
		inv.setStack(paramInt, paramItemStack);
	}

	public int getMaxCountPerStack() {
		return inv.getMaxCountPerStack();
	}

	public void markDirty() {
		inv.markDirty();
	}

	public boolean isValid(int slot, ItemStack stack, Boolean extract) {
		if(inv.getMaxCountPerStack() < stack.getCount() + inv.getStack(slot).getCount())return false;//Fix issues with mods that limit stacksize
		if(inv instanceof SidedInventory) {
			if(!canAccess(slot))return false;
			SidedInventory si = (SidedInventory) inv;
			if(extract == null) {
				return si.canExtract(slot, stack, dir) && si.canInsert(slot, stack, dir);
			}
			if(extract) {
				return si.canExtract(slot, stack, dir);
			} else {
				return si.canInsert(slot, stack, dir);
			}
		}
		return inv.isValid(slot, stack);
	}

	public int count(Item item) {
		return inv.count(item);
	}

	public boolean containsAny(Set<Item> items) {
		return inv.containsAny(items);
	}

	public Inventory getInventory() {
		return inv;
	}

	public boolean canAccess(int slot) {
		if(inv instanceof SidedInventory) {
			int[] slots = ((SidedInventory)inv).getAvailableSlots(dir);
			for (int i = 0; i < slots.length; i++) {
				if(slot == slots[i])return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return inv instanceof Nameable ? ((Nameable)inv).getName().asString() : "";
	}
}
