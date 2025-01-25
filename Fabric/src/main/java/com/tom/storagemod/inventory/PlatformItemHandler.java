package com.tom.storagemod.inventory;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import com.google.common.collect.MapMaker;

import com.tom.storagemod.inventory.IInventoryAccess.IInventory;

public class PlatformItemHandler implements Storage<ItemVariant>, IProxy {
	private static final Map<IInventory, PlatformItemHandler> WRAPPERS = new MapMaker().weakValues().makeMap();
	private IInventory access;

	private Storage<ItemVariant> getP() {
		return access.getInventoryAccess().getPlatformHandler();
	}

	public static PlatformItemHandler of(IInventory access) {
		return WRAPPERS.computeIfAbsent(access, PlatformItemHandler::new);
	}

	private PlatformItemHandler(IInventory access) {
		this.access = access;
	}

	@Override
	public void forEach(Consumer<? super StorageView<ItemVariant>> action) {
		getP().forEach(action);
	}

	@Override
	public Spliterator<StorageView<ItemVariant>> spliterator() {
		return getP().spliterator();
	}

	@Override
	public boolean supportsInsertion() {
		return getP().supportsInsertion();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getP().insert(resource, maxAmount, transaction);
	}

	@Override
	public boolean supportsExtraction() {
		return getP().supportsExtraction();
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return getP().extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return getP().iterator();
	}

	@Override
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		return getP().nonEmptyIterator();
	}

	@Override
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		return getP().nonEmptyViews();
	}

	@Override
	public long getVersion() {
		return getP().getVersion();
	}

	@Override
	public IInventoryAccess getRootHandler() {
		return access.getInventoryAccess().getRootHandler();
	}
}
