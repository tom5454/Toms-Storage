package com.tom.storagemod.inventory;

import java.util.Set;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PlatformProxyInventoryAccess implements PlatformInventoryAccess, ResourceHandler<ItemResource> {
	private boolean calling;
	private IInventoryAccess access;

	public PlatformProxyInventoryAccess(IInventoryAccess access) {
		this.access = access;
	}

	private ResourceHandler<ItemResource> getP() {
		return access.getPlatformHandler();
	}

	@Override
	public ResourceHandler<ItemResource> get() {
		return this;
	}

	@Override
	public boolean isValid(int slot, ItemResource stack) {
		if(calling)return false;
		calling = true;
		boolean v = getP().isValid(slot, stack);
		calling = false;
		return v;
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if(calling)return 0;
		calling = true;
		var is = getP().insert(index, resource, amount, transaction);
		calling = false;
		return is;
	}

	@Override
	public int insert(ItemResource resource, int amount, TransactionContext transaction) {
		if(calling)return 0;
		calling = true;
		var is = getP().insert(resource, amount, transaction);
		calling = false;
		return is;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if(calling)return 0;
		calling = true;
		var is = getP().extract(index, resource, amount, transaction);
		calling = false;
		return is;
	}

	@Override
	public int extract(ItemResource resource, int amount, TransactionContext transaction) {
		if(calling)return 0;
		calling = true;
		var is = getP().extract(resource, amount, transaction);
		calling = false;
		return is;
	}

	@Override
	public ItemResource getResource(int slot) {
		if(calling)return ItemResource.EMPTY;
		calling = true;
		ItemResource is = getP().getResource(slot);
		calling = false;
		return is;
	}

	@Override
	public long getAmountAsLong(int index) {
		if(calling)return 0;
		calling = true;
		var is = getP().getAmountAsLong(index);
		calling = false;
		return is;
	}

	@Override
	public int size() {
		if(calling)return 0;
		calling = true;
		int s = getP().size();
		calling = false;
		return s;
	}

	@Override
	public long getCapacityAsLong(int slot, ItemResource resource) {
		if(calling)return 0;
		calling = true;
		long s = getP().getCapacityAsLong(slot, resource);
		calling = false;
		return s;
	}

	@Override
	public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
		if (dejaVu.add(access))
			return access.getRootHandler(dejaVu);
		else
			return this;
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
