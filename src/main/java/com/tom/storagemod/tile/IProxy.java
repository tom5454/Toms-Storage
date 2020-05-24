package com.tom.storagemod.tile;

import java.util.function.Supplier;

import net.minecraftforge.items.IItemHandler;

public interface IProxy extends Supplier<IItemHandler> {

	public static IItemHandler resolve(IItemHandler in) {
		if(in instanceof IProxy)
			return resolve(((IProxy) in).get());
		else
			return in;
	}
}
