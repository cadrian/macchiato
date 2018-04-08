package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

class Array {

	private final Map<BigInteger, Object> array = new TreeMap<>();

	Object set(BigInteger index, Object value) {
		return array.put(index, value);
	}

	Object get(BigInteger index) {
		return array.get(index);
	}

}
