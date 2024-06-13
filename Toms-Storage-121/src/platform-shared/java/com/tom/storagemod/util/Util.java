package com.tom.storagemod.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Util {
	static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

	public static <T> Stream<T> stream(Iterator<T> itr) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(itr, Spliterator.ORDERED),
				false);
	}

	public static <T> Collector<T, ?, T> reducingWithCopy(T identity, BinaryOperator<T> op, UnaryOperator<T> copier) {
		return new CollectorImpl<>(
				() -> new Holder<>(identity),
				(a, t) -> { a.setValue(op.apply(a.value, t)); },
				(a, b) -> { a.setValue(op.apply(a.value, b.value)); return a; },
				a -> copier.apply(a.value),
				CH_NOID);
	}

	private static class Holder<T> {
		private T value;

		public Holder(T value) {
			this.value = value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}

	record CollectorImpl<T, A, R>(Supplier<A> supplier,
			BiConsumer<A, T> accumulator,
			BinaryOperator<A> combiner,
			Function<A, R> finisher,
			Set<Characteristics> characteristics
			) implements Collector<T, A, R> {
	}
}
