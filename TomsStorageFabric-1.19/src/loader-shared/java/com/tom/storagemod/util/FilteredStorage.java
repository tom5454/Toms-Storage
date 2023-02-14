package com.tom.storagemod.util;

import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.Iterators;

public class FilteredStorage extends FilteringStorage<ItemVariant> implements IProxy {
	private BlankVariantView<ItemVariant> nullSlot = new BlankVariantView<>(ItemVariant.blank(), 0);
	private List<ItemStack> filter;

	public FilteredStorage(Storage<ItemVariant> backingStorage, List<ItemStack> filter) {
		super(backingStorage);
		this.filter = filter;
	}

	private boolean inFilter(ItemVariant resource) {
		for (ItemStack f : filter) {
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
	public Iterator<StorageView<ItemVariant>> iterator() {
		return Iterators.transform(super.iterator(), sv -> {
			if(!inFilter(sv.getResource()))return nullSlot;
			return sv;
		});
	}

	@Override
	public Storage<ItemVariant> get() {
		return backingStorage.get();
	}
}
