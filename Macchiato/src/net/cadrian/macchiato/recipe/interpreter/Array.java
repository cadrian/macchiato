package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class Array {

	private final Map<BigInteger, Object> array = new TreeMap<>();

	Object set(final BigInteger index, final Object value) {
		return array.put(index, value);
	}

	Object get(final BigInteger index) {
		return array.get(index);
	}

	@Override
	public String toString() {
		return array.toString();
	}

}
