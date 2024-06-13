package com.tom.storagemod.util;

import java.util.Comparator;

public enum Priority {
	LOWEST,
	LOW,
	NORMAL,
	HIGH,
	HIGHEST
	;
	public static final Priority[] VALUES = values();

	public static interface IPriority {
		Priority getPriority();

		public static <T> Comparator<T> compare() {
			return Comparator.comparing(o -> {
				if (o instanceof IPriority p)return p.getPriority();
				return NORMAL;
			});
		}
	}
}
