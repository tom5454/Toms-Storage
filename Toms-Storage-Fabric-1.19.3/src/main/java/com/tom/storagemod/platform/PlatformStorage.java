package com.tom.storagemod.platform;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface PlatformStorage extends Storage<ItemVariant> {

	@Override
	default @Nullable StorageView<ItemVariant> exactView(TransactionContext transaction, ItemVariant resource) {
		return exactView(resource);
	}
}
