package com.tom.storagemod.util;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import com.google.common.collect.Iterators;

import com.tom.storagemod.platform.PlatformStorage;

public class FilteredStorage extends FilteringStorage<ItemVariant> implements IProxy, PlatformStorage {
	private BlankVariantView<ItemVariant> nullSlot = new BlankVariantView<>(ItemVariant.blank(), 0);
	private ItemPredicate filter;
	private boolean keepLastInSlot;

	public FilteredStorage(Storage<ItemVariant> backingStorage, ItemPredicate filter, boolean keepLastInSlot) {
		super(backingStorage);
		this.filter = filter;
		this.keepLastInSlot = keepLastInSlot;
	}

	private boolean inFilter(ItemVariant resource) {
		return filter.test(resource);
	}

	@Override
	protected boolean canInsert(ItemVariant resource) {
		return inFilter(resource);
	}

	@Override
	protected boolean canExtract(ItemVariant resource) {
		return inFilter(resource);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return Iterators.transform(super.iterator(), sv -> {
			if(!inFilter(sv.getResource()))return nullSlot;
			return keepLastInSlot ? new FilteringStorageView(sv) : sv;
		});
	}

	@Override
	public Storage<ItemVariant> get() {
		return backingStorage.get();
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(!canExtract(resource))return 0;
		if(keepLastInSlot) {
			Iterator<StorageView<ItemVariant>> itr = super.iterator();
			long amount = 0;
			while (itr.hasNext()) {
				StorageView<ItemVariant> sv = itr.next();
				if(!sv.isResourceBlank() && sv.getResource().equals(resource)) {
					long maxEx = Math.min(maxAmount - amount, sv.getAmount() - 1);
					amount += sv.extract(resource, maxEx, transaction);
					if (amount == maxAmount) break;
				}
			}
			return amount;
		}
		return backingStorage.get().extract(resource, maxAmount, transaction);
	}

	@Override
	public @Nullable StorageView<ItemVariant> exactView(ItemVariant resource) {
		if(keepLastInSlot) {
			StorageView<ItemVariant> v = super.exactView(resource);
			return v != null ? new FilteringStorageView(v) : v;
		} else
			return super.exactView(resource);
	}

	private class FilteringStorageView implements StorageView<ItemVariant> {
		private StorageView<ItemVariant> delegate;

		public FilteringStorageView(StorageView<ItemVariant> delegate) {
			this.delegate = delegate;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if(keepLastInSlot) {
				maxAmount = Math.min(delegate.getAmount() - 1, maxAmount);
				if(maxAmount < 1)return 0L;
			}
			return delegate.extract(resource, maxAmount, transaction);
		}

		@Override
		public boolean isResourceBlank() {
			return delegate.isResourceBlank() || (keepLastInSlot && delegate.getAmount() == 1L);
		}

		@Override
		public ItemVariant getResource() {
			return delegate.getResource();
		}

		@Override
		public long getAmount() {
			return keepLastInSlot ? delegate.getAmount() - 1L : delegate.getAmount();
		}

		@Override
		public long getCapacity() {
			return keepLastInSlot && !delegate.isResourceBlank() ? delegate.getCapacity() - 1L : delegate.getCapacity();
		}

		@Override
		public StorageView<ItemVariant> getUnderlyingView() {
			return delegate.getUnderlyingView();
		}
	}
}
