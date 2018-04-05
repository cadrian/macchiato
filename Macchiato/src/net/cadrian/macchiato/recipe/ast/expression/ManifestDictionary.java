package net.cadrian.macchiato.recipe.ast.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestDictionary implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(ManifestDictionary dictionary);
	}

	public static class Entry {
		private final TypedExpression key;
		private final Expression expression;

		Entry(final TypedExpression key, final Expression expression) {
			this.key = key;
			this.expression = expression;
		}

		public TypedExpression getKey() {
			return key;
		}

		public Expression getExpression() {
			return expression;
		}
	}

	private final List<Entry> expressions = new ArrayList<>();
	private final int position;

	public ManifestDictionary(final int position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(ManifestDictionary.class)) {
			return this;
		}
		return new CheckedExpression(this, type);
	}

	@Override
	public Class<?> getType() {
		return Map.class;
	}

	@Override
	public int position() {
		return position;
	}

	public void put(final TypedExpression key, final Expression expression) {
		expressions.add(new Entry(key, expression));
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
