package net.cadrian.macchiato.recipe.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {

	private final Map<String, Object> dictionary = new HashMap<>();

	public Object set(final String index, final Object value) {
		return dictionary.put(index, value);
	}

	public Object get(final String index) {
		return dictionary.get(index);
	}

	@Override
	public String toString() {
		return dictionary.toString();
	}

}
