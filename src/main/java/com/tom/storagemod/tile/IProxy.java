package com.tom.storagemod.tile;

import java.util.function.Supplier;

import net.minecraft.inventory.Inventory;

public interface IProxy extends Supplier<Inventory> {

	public static Inventory resolve(Inventory in) {
		if(in instanceof IProxy)
			return resolve(((IProxy) in).get());
		else
			return in;
	}
}
