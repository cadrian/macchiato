package net.cadrian.macchiato.recipe.interpreter;

import java.util.HashMap;
import java.util.Map;

class LocalContext extends Context {

	private final Context parent;
	private final Map<String, Object> local = new HashMap<>();

	public LocalContext(final Context parent) {
		this.parent = parent;
	}

	@Override
	Interpreter getInterpreter() {
		return parent.getInterpreter();
	}

	@Override
	boolean isNext() {
		return parent.isNext();
	}

	@Override
	void setNext(final boolean next) {
		parent.setNext(next);
	}

	@Override
	Track getTrack() {
		return parent.getTrack();
	}

	@Override
	AbstractEvent getEvent() {
		return parent.getEvent();
	}

	@Override
	void emit(final AbstractEvent event) {
		parent.emit(event);
	}

	@Override
	<T> T get(final String key) {
		@SuppressWarnings("unchecked")
		T result = (T) local.get(key);
		if (result == null) {
			result = parent.get(key);
		}
		return result;
	}

	@Override
	<T> T set(final String key, final T value) {
		@SuppressWarnings("unchecked")
		T result = (T) local.put(key, value);
		if (result == null) {
			result = parent.get(key);
		}
		return result;
	}

	@Override
	<T> T setGlobal(final String key, final T value) {
		return parent.setGlobal(key, value);
	}

}
