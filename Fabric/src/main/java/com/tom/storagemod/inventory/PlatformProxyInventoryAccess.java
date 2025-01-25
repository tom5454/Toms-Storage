package com.tom.storagemod.inventory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class PlatformProxyInventoryAccess implements PlatformInventoryAccess, Storage<ItemVariant> {
	private boolean calling;
	private IInventoryAccess access;

	public PlatformProxyInventoryAccess(IInventoryAccess access) {
		this.access = access;
	}

	private Storage<ItemVariant> getP() {
		return access.getPlatformHandler();
	}

	@Override
	public Storage<ItemVariant> get() {
		return this;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		if(calling)return 0L;
		calling = true;
		long v = getP().insert(resource, maxAmount, transaction);
		calling = false;
		return v;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		if(calling)return 0L;
		calling = true;
		long v = getP().extract(resource, maxAmount, transaction);
		calling = false;
		return v;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		if(calling)return Collections.emptyIterator();
		calling = true;
		var i = getP().iterator();
		calling = false;
		return i;
	}

	@Override
	public void forEach(Consumer<? super StorageView<ItemVariant>> action) {
		if(calling)return;
		calling = true;
		getP().forEach(action);
		calling = false;
	}

	@Override
	public Spliterator<StorageView<ItemVariant>> spliterator() {
		if(calling)return Spliterators.emptySpliterator();
		calling = true;
		var i = getP().spliterator();
		calling = false;
		return i;
	}

	@Override
	public boolean supportsInsertion() {
		if(calling)return false;
		calling = true;
		boolean v = getP().supportsInsertion();
		calling = false;
		return v;
	}

	@Override
	public boolean supportsExtraction() {
		if(calling)return false;
		calling = true;
		boolean v = getP().supportsExtraction();
		calling = false;
		return v;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		if(calling)return Collections.emptyIterator();
		calling = true;
		var i = getP().nonEmptyIterator();
		calling = false;
		return i;
	}

	@Override
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		if(calling)return Collections.emptyList();
		calling = true;
		var i = getP().nonEmptyViews();
		calling = false;
		return i;
	}

	@Override
	public long getVersion() {
		if(calling)return 0L;
		calling = true;
		var i = getP().getVersion();
		calling = false;
		return i;
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
