package com.tom.storagemod.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.screen.Property;

public class DataSlots extends Property {
	private IntConsumer c;
	private IntSupplier s;

	@Override
	public int get() {
		return s.getAsInt();
	}

	@Override
	public void set(int p_39402_) {
		c.accept(p_39402_);
	}

	private DataSlots(IntConsumer c, IntSupplier s) {
		this.c = c;
		this.s = s;
	}

	public static Property set(IntConsumer c) {
		return new DataSlots(c, () -> 0);
	}

	public static Property get(IntSupplier s) {
		return new DataSlots(a -> {}, s);
	}

	public static Property create(IntConsumer c, IntSupplier s) {
		return new DataSlots(c, s);
	}
}
