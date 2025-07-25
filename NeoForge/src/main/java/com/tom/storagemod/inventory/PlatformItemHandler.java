package com.tom.storagemod.inventory;

import java.util.Set;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.inventory.IInventoryAccess.IInventory;

public class PlatformItemHandler implements IItemHandler, IProxy {
	private IInventory access;

	private IItemHandler getP() {
		return access.getInventoryAccess().getPlatformHandler();
	}

	public PlatformItemHandler(IInventory access) {
		this.access = access;
	}

	@Override
	public int getSlots() {
		return getP().getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getP().getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return getP().insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return getP().extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return getP().getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return getP().isItemValid(slot, stack);
	}

	@Override
	public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
		return access.getInventoryAccess().getRootHandler(dejaVu);
	}
}
