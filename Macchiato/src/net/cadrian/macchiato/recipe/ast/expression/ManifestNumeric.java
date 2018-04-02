package net.cadrian.macchiato.recipe.ast.expression;

import java.math.BigInteger;

import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestNumeric implements TypedExpression<BigInteger> {

	public static interface Visitor extends Node.Visitor {
		void visit(ManifestNumeric manifestNumeric);
	}

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

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
