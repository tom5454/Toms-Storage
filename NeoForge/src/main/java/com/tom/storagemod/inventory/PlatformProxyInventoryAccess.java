package com.tom.storagemod.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class PlatformProxyInventoryAccess implements PlatformInventoryAccess, IItemHandler {
	private boolean calling;
	private IInventoryAccess access;

	public PlatformProxyInventoryAccess(IInventoryAccess access) {
		this.access = access;
	}

	private IItemHandler getP() {
		return access.getPlatformHandler();
	}

	@Override
	public IItemHandler get() {
		return this;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if(calling)return false;
		calling = true;
		boolean v = getP().isItemValid(slot, stack);
		calling = false;
		return v;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(calling)return stack;
		calling = true;
		ItemStack is = getP().insertItem(slot, stack, simulate);
		calling = false;
		return is;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(calling)return ItemStack.EMPTY;
		calling = true;
		ItemStack is = getP().getStackInSlot(slot);
		calling = false;
		return is;
	}

	@Override
	public int getSlots() {
		if(calling)return 0;
		calling = true;
		int s = getP().getSlots();
		calling = false;
		return s;
	}

	@Override
	public int getSlotLimit(int slot) {
		if(calling)return 0;
		calling = true;
		int s = getP().getSlotLimit(slot);
		calling = false;
		return s;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(calling)return ItemStack.EMPTY;
		calling = true;
		ItemStack is = getP().extractItem(slot, amount, simulate);
		calling = false;
		return is;
	}

	@Override
	public IInventoryAccess getRootHandler() {
		return access.getRootHandler();
	}

	@Override
	public IInventoryChangeTracker tracker() {
		if(calling)return InventoryChangeTracker.NULL;
		calling = true;
		var c = access.tracker();
		calling = false;
		return c;
	}
}
