package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;
import java.util.function.Supplier;

import com.tom.storagemod.inventory.TerminalItemStack;

public enum SortingTypes {
	AMOUNT(ComparatorAmount::new),
	NAME(ComparatorName::new),
	MOD(ComparatorMod::new),
	SPACE_EFFICIENCY(ComparatorSpaceEfficiency::new),
	ID(ComparatorID::new),
	TYPE(ComparatorType::new),
	;

	public static final SortingTypes[] VALUES = values();
	private final Supplier<Comparator<TerminalItemStack>> factory;

	private SortingTypes(Supplier<Comparator<TerminalItemStack>> factory) {
		this.factory = factory;
	}

	public Comparator<TerminalItemStack> create(boolean reversed) {
		return reversed ? factory.get().reversed() : factory.get();
	}
}