package net.cadrian.macchiato.recipe.ast.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;
import net.cadrian.macchiato.recipe.interpreter.Dictionary;

public class ManifestDictionary implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(ManifestDictionary manifestDictionary);
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
		return Dictionary.class;
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

	public List<Entry> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

}
