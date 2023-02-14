package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

public class MergedStorage extends CombinedStorage<ItemVariant, Storage<ItemVariant>> {
	private Set<Storage<ItemVariant>> dupCheck = new HashSet<>();

	public MergedStorage() {
		super(new ArrayList<>());
	}

	public Collection<Storage<ItemVariant>> getStorages() {
		return parts;
	}

	public void add(Storage<ItemVariant> storage) {
		if(storage instanceof MergedStorage str) {
			str.parts.forEach(this::add);
			return;
		}
		if(dupCheck.add(storage))
			this.parts.add(storage);
	}

	public void clear() {
		parts.clear();
		dupCheck.clear();
	}
}
