package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.Config;

public class MultiItemHandler implements IItemHandler {
	private List<IItemHandler> handlers = new ArrayList<>();
	private List<ItemStack[]> dupDetector = new ArrayList<>();
	private int[] invSizes = new int[0];
	private int invSize;
	private boolean calling;

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if(calling)return false;
		if(slot >= invSize)return false;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				boolean r = handlers.get(i).isItemValid(slot, stack);
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
				ItemStack s = handlers.get(i).insertItem(slot, stack, simulate);
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
				ItemStack s = handlers.get(i).getStackInSlot(slot);
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
				int r = handlers.get(i).getSlotLimit(slot);
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
				ItemStack s = handlers.get(i).extractItem(slot, amount, simulate);
				calling = false;
				return s;
			}
		}
		calling = false;
		return ItemStack.EMPTY;
	}

	public List<IItemHandler> getHandlers() {
		return handlers;
	}

	public void refresh() {
		dupDetector.clear();
		if(invSizes.length != handlers.size())invSizes = new int[handlers.size()];
		invSize = 0;
		for (int i = 0; i < invSizes.length; i++) {
			IItemHandler ih = handlers.get(i);
			if(ih == null)invSizes[i] = 0;
			else {
				int s = ih.getSlots();
				invSizes[i] = s;
				invSize += s;
			}
		}
	}

	public boolean contains(Object o) {
		return handlers.contains(o);
	}

	public void add(IItemHandler e) {
		if(checkInv(e))
			handlers.add(e);
	}

	private boolean checkInv(IItemHandler h) {
		int len = Math.min(Config.get().invDupScanSize, h.getSlots());
		if(len == 0)return true;
		ItemStack[] is = new ItemStack[len];
		for(int i = 0;i<len;i++) {
			is[i] = h.getStackInSlot(i);
		}

		for (ItemStack[] st : dupDetector) {
			int l = Math.min(len, st.length);
			for (int i = 0; i < l; i++) {
				ItemStack item = st[i];
				if(!item.isEmpty() && item == is[i])
					return false;
			}
		}
		dupDetector.add(is);
		return true;
	}

	public void clear() {
		invSize = 0;
		handlers.clear();
		dupDetector.clear();
	}
}
