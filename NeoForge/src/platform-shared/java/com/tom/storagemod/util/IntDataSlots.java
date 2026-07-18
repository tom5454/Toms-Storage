package com.tom.storagemod.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.world.inventory.ContainerData;

public class IntDataSlots implements ContainerData {
	private static final int MASK = 0xFFFF;
	private IntConsumer c;
	private IntSupplier s;
	private Runnable onUpdate;
	private int value;

	@Override
	public int get(int off) {
		int bitOff = off << 4;
		return (s.getAsInt() & (MASK << bitOff)) >> bitOff;
	}

	@Override
	public void set(int off, int val) {
		int bitOff = off << 4;
		int newVal = (value & ~(MASK << bitOff)) | ((val & MASK) << bitOff);
		c.accept(newVal);
		value = newVal;
		if(onUpdate != null)onUpdate.run();
	}

	@Override
	public int getCount() {
		return 2;
	}

	public static IntDataSlots create(IntConsumer c, IntSupplier s) {
		return new IntDataSlots(c, s);
	}

	public IntDataSlots(IntConsumer c, IntSupplier s) {
		this.c = c;
		this.s = s;
	}

	public IntDataSlots onUpdate(Runnable r) {
		this.onUpdate = r;
		return this;
	}
}
