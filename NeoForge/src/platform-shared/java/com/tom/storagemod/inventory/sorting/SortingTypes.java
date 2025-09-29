package com.tom.storagemod.inventory.sorting;

import java.util.function.Function;

public enum SortingTypes {
	AMOUNT(ComparatorAmount::new),
	NAME(ComparatorName::new),
	BY_MOD(ComparatorModName::new),
	SPACE_EFFICIENCY(ComparatorSpaceEfficiency::new),
	ID(ComparatorID::new),
	TYPE(ComparatorType::new),
	;
	public static final SortingTypes[] VALUES = values();
	private final Function<Boolean, StoredItemStackComparator> factory;
	private SortingTypes(Function<Boolean, StoredItemStackComparator> factory) {
		this.factory = factory;
	}

	public StoredItemStackComparator create(boolean rev) {
		return factory.apply(rev);
	}
}