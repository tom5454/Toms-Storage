package com.tom.storagemod.inventory;

import java.util.Arrays;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

public class PlatformMultiInventoryAccess extends MultiInventoryAccess implements IItemHandler {
	private int[] offsets = new int[0];
	private int invSize, offsetsSize;
	private boolean calling;

	@Override
	public IItemHandler get() {
		return this;
	}

	private int findInventory(int slot) {
		int arrayIndex = Arrays.binarySearch(offsets, 0, offsetsSize, slot);
		if (arrayIndex < 0) {
			arrayIndex = -arrayIndex - 2;
		}
		return arrayIndex;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if(calling)return false;
		if(slot >= invSize)return false;
		calling = true;

		int arrayIndex = findInventory(slot);

		boolean r = getHandler(arrayIndex).isItemValid(slot - offsets[arrayIndex], stack);
		calling = false;
		return r;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(calling)return stack;
		if(slot >= invSize)return stack;
		calling = true;

		int arrayIndex = findInventory(slot);

		ItemStack s = getHandler(arrayIndex).insertItem(slot - offsets[arrayIndex], stack, simulate);
		calling = false;
		return s;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(calling)return ItemStack.EMPTY;
		if(slot >= invSize)return ItemStack.EMPTY;
		calling = true;

		int arrayIndex = findInventory(slot);

		ItemStack s = getHandler(arrayIndex).getStackInSlot(slot - offsets[arrayIndex]);
		calling = false;
		return s;
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

		int arrayIndex = findInventory(slot);

		int r = getHandler(arrayIndex).getSlotLimit(slot - offsets[arrayIndex]);
		calling = false;
		return r;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(calling)return ItemStack.EMPTY;
		if(slot >= invSize)return ItemStack.EMPTY;
		calling = true;

		int arrayIndex = findInventory(slot);

		ItemStack s = getHandler(arrayIndex).extractItem(slot - offsets[arrayIndex], amount, simulate);
		calling = false;
		return s;
	}

	@Override
	public void clear() {
		super.clear();
		invSize = 0;
	}

	@Override
	public void refresh() {
		if(offsets.length != connected.size())offsets = new int[connected.size()];
		invSize = 0;
		int hOff = 0;
		for (int i = 0; i < offsets.length; i++) {
			IItemHandler ih = getHandler(i);
			if(ih == null) {
				hOff++;
			} else {
				int s = ih.getSlots();
				offsets[i - hOff] = invSize;
				invSize += s;
			}
		}
		offsetsSize = offsets.length - hOff;
	}

	private IItemHandler getHandler(int i) {
		IItemHandler h = connected.get(i).getPlatformHandler();
		if (h == null)return EmptyItemHandler.INSTANCE;
		return h;
	}
}
