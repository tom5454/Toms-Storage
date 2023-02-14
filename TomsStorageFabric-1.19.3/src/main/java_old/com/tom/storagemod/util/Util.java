package com.tom.storagemod.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Util {

	public static <T> Stream<T> stream(Iterator<T> itr) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(itr, Spliterator.ORDERED),
				false);
	}
}
