package com.tom.storagemod.inventory;

import java.util.Arrays;

import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PlatformMultiInventoryAccess extends MultiInventoryAccess implements ResourceHandler<ItemResource> {
	private int[] offsets = new int[0];
	private int invSize, offsetsSize;
	private boolean calling;

	@Override
	public ResourceHandler<ItemResource> get() {
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
	public boolean isValid(int slot, ItemResource stack) {
		if(calling)return false;
		if(slot >= invSize)return false;
		calling = true;

		int arrayIndex = findInventory(slot);
		int invSlot = slot - offsets[arrayIndex];

		boolean r = getHandler(arrayIndex, invSlot).isValid(invSlot, stack);
		calling = false;
		return r;
	}

	@Override
	public int size() {
		return invSize;
	}

	@Override
	public long getCapacityAsLong(int slot, ItemResource resource) {
		if(calling)return 0;
		if(slot >= invSize)return 0;
		calling = true;

		int arrayIndex = findInventory(slot);
		int invSlot = slot - offsets[arrayIndex];

		long r = getHandler(arrayIndex, invSlot).getCapacityAsLong(invSlot, resource);
		calling = false;
		return r;
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
			ResourceHandler<ItemResource> ih = getHandler(i, 0);
			int s = ih.size();
			if (s == 0) {
				hOff++;
			} else {
				offsets[i - hOff] = invSize;
				invSize += s;
			}
		}
		offsetsSize = offsets.length - hOff;
	}

	private ResourceHandler<ItemResource> getHandler(int i, int invSlot) {
		ResourceHandler<ItemResource> h = connected.get(i).getPlatformHandler();
		if (h == null)return EmptyResourceHandler.instance();
		if (invSlot < h.size())
			return h;
		return EmptyResourceHandler.instance();
	}

	@Override
	public ItemResource getResource(int slot) {
		if(calling)return ItemResource.EMPTY;
		if(slot >= invSize)return ItemResource.EMPTY;
		calling = true;

		int arrayIndex = findInventory(slot);
		int invSlot = slot - offsets[arrayIndex];

		ItemResource s = getHandler(arrayIndex, invSlot).getResource(invSlot);
		calling = false;
		return s;
	}

	@Override
	public long getAmountAsLong(int slot) {
		if(calling)return 0L;
		if(slot >= invSize)return 0L;
		calling = true;

		int arrayIndex = findInventory(slot);
		int invSlot = slot - offsets[arrayIndex];

		long s = getHandler(arrayIndex, invSlot).getAmountAsLong(invSlot);
		calling = false;
		return s;
	}

	@Override
	public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
		if(calling)return 0;
		if(slot >= invSize)return 0;
		calling = true;

		int arrayIndex = findInventory(slot);
		int invSlot = slot - offsets[arrayIndex];

		int s = getHandler(arrayIndex, invSlot).insert(invSlot, resource, amount, transaction);
		calling = false;
		return s;
	}

	@Override
	public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
		if(calling)return 0;
		if(slot >= invSize)return 0;
		calling = true;

		int arrayIndex = findInventory(slot);
		int invSlot = slot - offsets[arrayIndex];

		int s = getHandler(arrayIndex, invSlot).extract(invSlot, resource, amount, transaction);
		calling = false;
		return s;
	}
}
