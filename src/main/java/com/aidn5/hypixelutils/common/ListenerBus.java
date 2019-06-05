package com.aidn5.hypixelutils.common;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ListenerBus<T extends EventListener> {
	private Set<T> listeners = new HashSet<T>();

	public void register(T listener) {
		listeners.add(Objects.requireNonNull(listener));
	}

	public void unregister(T listener) {
		listeners.remove(Objects.requireNonNull(listener));
	}

	protected Set<T> getListeners() {
		return listeners;
	}
}
