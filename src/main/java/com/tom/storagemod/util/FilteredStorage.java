package com.tom.storagemod.util;

import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Iterators;

public class FilteredStorage extends FilteringStorage<ItemVariant> implements IProxy {
	private BlankVariantView<ItemVariant> nullSlot = new BlankVariantView<>(ItemVariant.blank(), 0);
	private Inventory filter;

	public FilteredStorage(Storage<ItemVariant> backingStorage, Inventory filter) {
		super(backingStorage);
		this.filter = filter;
	}

	private boolean inFilter(ItemVariant resource) {
		for (int i = 0;i<filter.size();i++) {
			ItemStack f = filter.getStack(i);
			if(f.isEmpty())continue;
			if(resource.matches(f))return true;
		}
		return false;
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
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return Iterators.transform(super.iterator(transaction), sv -> {
			if(!inFilter(sv.getResource()))return nullSlot;
			return sv;
		});
	}

	@Override
	public Storage<ItemVariant> get() {
		return backingStorage.get();
	}
}
