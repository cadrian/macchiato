package net.cadrian.macchiato.recipe.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {

	private final Map<String, Object> array = new HashMap<>();

	Object set(String index, Object value) {
		return array.put(index, value);
	}

	Object get(String index) {
		return array.get(index);
	}

}
