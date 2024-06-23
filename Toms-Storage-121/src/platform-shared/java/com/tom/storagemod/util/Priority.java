package com.tom.storagemod.util;

import java.util.Comparator;
import java.util.function.Function;

public enum Priority {
	LOWEST,
	LOW,
	NORMAL,
	HIGH,
	HIGHEST
	;
	public static final Priority[] VALUES = values();

	public static interface IPriority {
		public static final Function<Object, Priority> GETTER = o -> {
			if (o instanceof IPriority p)return p.getPriority();
			return NORMAL;
		};

		Priority getPriority();

		public static <T> Comparator<T> compare() {
			return Comparator.comparing(GETTER);
		}
	}
}
