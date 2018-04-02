package net.cadrian.macchiato.recipe.ast;

import java.util.HashMap;
import java.util.Map;

public class Dictionary implements Expression {

	private final Map<TypedExpression<Comparable<?>>, Expression> expressions = new HashMap<>();
	private final int position;

	public Dictionary(final int position) {
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
