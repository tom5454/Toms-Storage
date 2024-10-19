package com.tom.storagemod.util;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;

public enum Priority {
	LOWEST,
	LOW,
	NORMAL,
	HIGH,
	HIGHEST
	;
	public static final Priority[] VALUES = values();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Codec<Priority> CODEC = StringRepresentable.fromEnum((Supplier) Priority::values);

	public static interface IPriority {
		public static final Function<Object, Priority> GETTER = IPriority::get;

		Priority getPriority();

		public static <T> Comparator<T> compare() {
			return Comparator.comparing(GETTER);
		}

		public static Priority get(Object o) {
			if (o instanceof IPriority p)return p.getPriority();
			return NORMAL;
		}
	}

	public Priority add(Priority other) {
		if (this == NORMAL)return other;
		if (other == NORMAL)return this;
		return fromSorting(getSorting() + other.getSorting());
	}

	public int getSorting() {
		return ordinal() - 2;
	}

	public static Priority fromSorting(int sorting) {
		return Priority.values()[Mth.clamp(sorting + 2, 0, Priority.values().length - 1)];
	}
}
