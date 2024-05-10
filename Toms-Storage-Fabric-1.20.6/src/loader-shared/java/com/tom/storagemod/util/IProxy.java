package com.tom.storagemod.util;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public interface IProxy extends Supplier<Storage<ItemVariant>> {

	public static Storage<ItemVariant> resolve(Storage<ItemVariant> in) {
		if(in instanceof IProxy)
			return resolve(((IProxy) in).get());
		else
			return in;
	}
}
