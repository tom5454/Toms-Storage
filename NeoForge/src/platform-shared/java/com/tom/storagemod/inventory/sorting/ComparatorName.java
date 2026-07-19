package com.tom.storagemod.inventory.sorting;

import java.util.Comparator;

import com.tom.storagemod.inventory.TerminalItemStack;

public class ComparatorName implements Comparator<TerminalItemStack> {
	@Override
	public int compare(TerminalItemStack in1, TerminalItemStack in2) {
		return in1.getDisplayName().compareTo(in2.getDisplayName());
	}
}