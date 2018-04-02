package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public class Array implements TypedExpression<Array> {

	private final List<Expression> expressions = new ArrayList<>();
	private final int position;

	public Array(final int position) {
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(Array.class)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return new CheckedExpression<T>(this, type);
	}

	@Override
	public int position() {
		return position;
	}

	public void add(final Expression expression) {
		expressions.add(expression);
	}

}
