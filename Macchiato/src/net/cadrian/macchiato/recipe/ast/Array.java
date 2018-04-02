package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public class Array implements Expression {

	private final List<Expression> expressions = new ArrayList<>();
	private final int position;

	public Array(final int position) {
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
	}

	@Override
	public int position() {
		return position;
	}

}
