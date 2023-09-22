package com.tom.storagemod.platform;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SavedDataFactory<T extends SavedData> {
	private final Factory<T> fact;
	private final String id;

	public SavedDataFactory(Function<CompoundTag, T> loader, Supplier<T> factory, String id) {
		fact = new Factory<>(factory, loader, DataFixTypes.LEVEL);
		this.id = id;
	}

	public T get(DimensionDataStorage storage) {
		return storage.computeIfAbsent(fact, id);
	}
}
