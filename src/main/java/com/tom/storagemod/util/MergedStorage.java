package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.Collection;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

public class MergedStorage extends CombinedStorage<ItemVariant, Storage<ItemVariant>> {

	public MergedStorage() {
		super(new ArrayList<>());
	}

	public Collection<Storage<ItemVariant>> getStorages() {
		return parts;
	}

	public void add(Storage<ItemVariant> storage) {
		this.parts.add(storage);
	}

	public void clear() {
		parts.clear();
	}
}
