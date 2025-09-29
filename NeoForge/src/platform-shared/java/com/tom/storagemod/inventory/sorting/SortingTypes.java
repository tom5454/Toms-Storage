package com.tom.storagemod.inventory.sorting;

import java.util.function.Function;

public enum SortingTypes {
	AMOUNT(ComparatorAmount::new),
	NAME(ComparatorName::new),
	BY_MOD(ComparatorModName::new),
	;
	public static final SortingTypes[] VALUES = values();
	private final Function<Boolean, IStoredItemStackComparator> factory;
	private SortingTypes(Function<Boolean, IStoredItemStackComparator> factory) {
		this.factory = factory;
	}

	public IStoredItemStackComparator create(boolean rev) {
		return factory.apply(rev);
	}
}