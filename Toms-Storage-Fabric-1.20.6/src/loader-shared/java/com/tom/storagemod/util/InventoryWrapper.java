package com.tom.storagemod.util;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryWrapper {
	private static final SimpleContainer DUMMY = new SimpleContainer(0);
	private final Container inv;
	private final Direction dir;

	public InventoryWrapper(Container inv, Direction dir) {
		if(inv == null)throw new NullPointerException("inv is null");
		this.inv = inv;
		this.dir = dir;
	}

	protected InventoryWrapper() {
		this.inv = DUMMY;
		this.dir = Direction.DOWN;
	}

	public int size() {
		return inv.getContainerSize();
	}

	public boolean isEmpty() {
		return inv.isEmpty();
	}

	public ItemStack getStack(int paramInt) {
		return inv.getItem(paramInt);
	}

	public ItemStack removeStack(int paramInt1, int paramInt2) {
		return inv.removeItem(paramInt1, paramInt2);
	}

	public ItemStack removeStack(int paramInt) {
		return inv.removeItemNoUpdate(paramInt);
	}

	public void setStack(int paramInt, ItemStack paramItemStack) {
		inv.setItem(paramInt, paramItemStack);
	}

	public int getMaxCountPerStack() {
		return inv.getMaxStackSize();
	}

	public void markDirty() {
		inv.setChanged();
	}

	public boolean isValid(int slot, ItemStack stack, Boolean extract) {
		if(inv.getMaxStackSize() < stack.getCount() + inv.getItem(slot).getCount())return false;//Fix issues with mods that limit stacksize
		if(inv instanceof WorldlyContainer) {
			if(!canAccess(slot))return false;
			WorldlyContainer si = (WorldlyContainer) inv;
			if(extract == null) {
				return si.canTakeItemThroughFace(slot, stack, dir) && si.canPlaceItemThroughFace(slot, stack, dir);
			}
			if(extract) {
				return si.canTakeItemThroughFace(slot, stack, dir);
			} else {
				return si.canPlaceItemThroughFace(slot, stack, dir);
			}
		}
		return inv.canPlaceItem(slot, stack);
	}

	public int count(Item item) {
		return inv.countItem(item);
	}

	public boolean containsAny(Set<Item> items) {
		return inv.hasAnyOf(items);
	}

	public Container getInventory() {
		return inv;
	}

	public boolean canAccess(int slot) {
		if(inv instanceof WorldlyContainer) {
			int[] slots = ((WorldlyContainer)inv).getSlotsForFace(dir);
			for (int i = 0; i < slots.length; i++) {
				if(slot == slots[i])return true;
			}
		}
		return false;
	}
}
