package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;
import java.util.function.Supplier;

import com.tom.storagemod.inventory.StoredItemStack;

public enum SortingTypes {
	AMOUNT(ComparatorAmount::new),
	NAME(ComparatorName::new),
	MOD(ComparatorMod::new),
	SPACE_EFFICIENCY(ComparatorSpaceEfficiency::new),
	ID(ComparatorID::new),
	TYPE(ComparatorType::new),
	;

	public static final SortingTypes[] VALUES = values();
	private final Supplier<Comparator<StoredItemStack>> factory;

	private SortingTypes(Supplier<Comparator<StoredItemStack>> factory) {
		this.factory = factory;
	}

	public Comparator<StoredItemStack> create(boolean reversed) {
		return reversed ? factory.get().reversed() : factory.get();
	}
}