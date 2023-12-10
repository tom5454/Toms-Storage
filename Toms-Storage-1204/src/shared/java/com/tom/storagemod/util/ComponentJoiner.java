package com.tom.storagemod.util;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ComponentJoiner {
	static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

	private MutableComponent value;
	private Component delimiter;

	public ComponentJoiner(Component delimiter) {
		this.delimiter = delimiter;
	}

	public static Collector<Component, ComponentJoiner, Component> joining(Component empty, Component delimiter) {
		return new Collector<>() {

			@Override
			public Supplier<ComponentJoiner> supplier() {
				return () -> new ComponentJoiner(delimiter);
			}

			@Override
			public Function<ComponentJoiner, Component> finisher() {
				return c -> c.value != null ? c.value : empty;
			}

			@Override
			public BinaryOperator<ComponentJoiner> combiner() {
				return ComponentJoiner::merge;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return CH_NOID;
			}

			@Override
			public BiConsumer<ComponentJoiner, Component> accumulator() {
				return ComponentJoiner::add;
			}
		};
	}

	private MutableComponent prepareBuilder() {
		if (value != null) {
			value.append(delimiter);
		} else {
			value = Component.empty();
		}
		return value;
	}

	public ComponentJoiner add(Component newElement) {
		prepareBuilder().append(newElement);
		return this;
	}

	public ComponentJoiner merge(ComponentJoiner other) {
		if (other.value != null) {
			prepareBuilder().append(other.value.copy());
		}
		return this;
	}
}
