package com.tom.storagemod.util;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.world.inventory.DataSlot;

public class DataSlots extends DataSlot {
	private IntConsumer c;
	private IntSupplier s;
	private Runnable onUpdate;

	@Override
	public int get() {
		return s.getAsInt();
	}

	@Override
	public void set(int p_39402_) {
		c.accept(p_39402_);
		if(onUpdate != null)onUpdate.run();
	}

	private DataSlots(IntConsumer c, IntSupplier s) {
		this.c = c;
		this.s = s;
	}

	public static DataSlots set(IntConsumer c) {
		return new DataSlots(c, () -> 0);
	}

	public static DataSlots get(IntSupplier s) {
		return new DataSlots(a -> {}, s);
	}

	public static DataSlots create(IntConsumer c, IntSupplier s) {
		return new DataSlots(c, s);
	}

	public DataSlots onUpdate(Runnable r) {
		this.onUpdate = r;
		return this;
	}
}
