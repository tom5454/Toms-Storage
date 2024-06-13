package com.tom.storagemod.util;

import java.util.function.Supplier;

import net.neoforged.neoforge.items.IItemHandler;

public interface IProxy extends Supplier<IItemHandler> {

	public static IItemHandler resolve(IItemHandler in) {
		if(in instanceof IProxy p)
			return resolve(p.get());
		else
			return in;
	}

}
