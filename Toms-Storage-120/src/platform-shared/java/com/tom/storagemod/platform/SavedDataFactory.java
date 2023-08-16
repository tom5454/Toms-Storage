package com.tom.storagemod.platform;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SavedDataFactory<T extends SavedData> {
	private final Function<CompoundTag, T> loader;
	private final Supplier<T> factory;
	private final String id;

	public SavedDataFactory(Function<CompoundTag, T> loader, Supplier<T> factory, String id) {
		this.loader = loader;
		this.factory = factory;
		this.id = id;
	}

	public T get(DimensionDataStorage storage) {
		return storage.computeIfAbsent(loader, factory, id);
	}
}
