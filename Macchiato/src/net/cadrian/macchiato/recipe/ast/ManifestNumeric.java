package net.cadrian.macchiato.recipe.ast;

import java.math.BigInteger;

public class ManifestNumeric implements TypedExpression<BigInteger> {

	private final BigInteger value;
	private final int position;

	public ManifestNumeric(final int position, final BigInteger value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(BigInteger.class)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return null;
	}

	public BigInteger getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

}
