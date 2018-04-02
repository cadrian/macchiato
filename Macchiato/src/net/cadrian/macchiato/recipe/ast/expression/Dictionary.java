package net.cadrian.macchiato.recipe.ast.expression;

import java.util.ArrayList;
import java.util.List;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class Dictionary implements TypedExpression<Dictionary> {

	public static interface Visitor extends Node.Visitor {
		void visit(Dictionary dictionary);
	}

	public static class Entry {
		private final TypedExpression<Comparable<?>> key;
		private final Expression expression;

		Entry(final TypedExpression<Comparable<?>> key, final Expression expression) {
			this.key = key;
			this.expression = expression;
		}

		public TypedExpression<Comparable<?>> getKey() {
			return key;
		}

		public Expression getExpression() {
			return expression;
		}
	}

	private final List<Entry> expressions = new ArrayList<>();
	private final int position;

	public Dictionary(final int position) {
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(Dictionary.class)) {
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

	public void put(final TypedExpression<Comparable<?>> key, final Expression expression) {
		expressions.add(new Entry(key, expression));
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
