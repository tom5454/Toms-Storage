package com.tom.storagemod.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

public class PlatformMultiInventoryAccess extends MultiInventoryAccess implements IItemHandler {
	private int[] invSizes = new int[0];
	private int invSize;
	private boolean calling;

	@Override
	public IItemHandler get() {
		return this;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if(calling)return false;
		if(slot >= invSize)return false;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				boolean r = getHandler(i).isItemValid(slot, stack);
				calling = false;
				return r;
			}
		}
		calling = false;
		return false;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(calling)return stack;
		if(slot >= invSize)return stack;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				ItemStack s = getHandler(i).insertItem(slot, stack, simulate);
				calling = false;
				return s;
			}
		}
		calling = false;
		return stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(calling)return ItemStack.EMPTY;
		if(slot >= invSize)return ItemStack.EMPTY;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				ItemStack s = getHandler(i).getStackInSlot(slot);
				calling = false;
				return s;
			}
		}
		calling = false;
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlots() {
		return invSize;
	}

	@Override
	public int getSlotLimit(int slot) {
		if(calling)return 0;
		if(slot >= invSize)return 0;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				int r = getHandler(i).getSlotLimit(slot);
				calling = false;
				return r;
			}
		}
		calling = false;
		return 0;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(calling)return ItemStack.EMPTY;
		if(slot >= invSize)return ItemStack.EMPTY;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				ItemStack s = getHandler(i).extractItem(slot, amount, simulate);
				calling = false;
				return s;
			}
		}
		calling = false;
		return ItemStack.EMPTY;
	}

	@Override
	public void refresh() {
		if(invSizes.length != connected.size())invSizes = new int[connected.size()];
		invSize = 0;
		for (int i = 0; i < invSizes.length; i++) {
			IItemHandler ih = getHandler(i);
			if(ih == null)invSizes[i] = 0;
			else {
				int s = ih.getSlots();
				invSizes[i] = s;
				invSize += s;
			}
		}
	}

	private IItemHandler getHandler(int i) {
		IItemHandler h = connected.get(i).getPlatformHandler();
		if (h == null)return EmptyItemHandler.INSTANCE;
		return h;
	}
}
