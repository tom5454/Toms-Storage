package com.tom.storagemod.util;

import java.util.EnumSet;
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
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Util {
	static final Set<Collector.Characteristics> CH_UNORDERED_NOID = EnumSet.of(Characteristics.UNORDERED);

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
				CH_UNORDERED_NOID);
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

	public static void loadItems(Container cnt, String id, ValueInput compound) {
		ValueInput.TypedInputList<ItemStackWithSlot> pContainerNbt = compound.listOrEmpty(id, ItemStackWithSlot.CODEC);

		for(int i = 0; i < cnt.getContainerSize(); ++i) {
			cnt.setItem(i, ItemStack.EMPTY);
		}

		for (final ItemStackWithSlot itemStackWithSlot : pContainerNbt) {
			if (itemStackWithSlot.isValidInContainer(cnt.getContainerSize())) {
				cnt.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
			}
		}
	}

	public static void storeItems(Container cnt, String id, ValueOutput compound) {
		ValueOutput.TypedOutputList<ItemStackWithSlot> output = compound.list(id, ItemStackWithSlot.CODEC);

		for (int i = 0; i < cnt.getContainerSize(); ++i) {
			ItemStack itemstack = cnt.getItem(i);
			if (!itemstack.isEmpty()) {
				output.add(new ItemStackWithSlot(i, itemstack));
			}
		}
	}
}
