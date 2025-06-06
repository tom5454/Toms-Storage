package com.tom.storagemod.api;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class EventHandler<T> {
	private final Function<T[], T> invokerFactory;
	private final Object lock = new Object();
	private final Class<T> eventType;
	private List<T> handlers;
	private T invoker;

	public EventHandler(Class<T> eventType, Function<T[], T> invokerFactory) {
		this.invokerFactory = invokerFactory;
		this.eventType = eventType;
		this.handlers = new ArrayList<>();
		update();
	}

	public void register(T listener) {
		Objects.requireNonNull(listener, "Tried to register a null listener!");

		synchronized (lock) {
			handlers.add(listener);
			update();
		}
	}

	@SuppressWarnings("unchecked")
	private void update() {
		invoker = invokerFactory.apply(handlers.toArray(i -> (T[]) Array.newInstance(eventType, i)));
	}

	public T invoker() {
		return invoker;
	}
}
