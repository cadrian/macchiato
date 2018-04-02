package net.cadrian.macchiato.recipe.ast.expression;

import java.util.ArrayList;
import java.util.List;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class Array implements TypedExpression<Array> {

	public static interface Visitor extends Node.Visitor {
		void visit(Array array);
	}

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

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
