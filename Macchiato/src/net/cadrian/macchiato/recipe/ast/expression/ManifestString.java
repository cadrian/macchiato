package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestString implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visit(ManifestString manifestString);
	}

	private final String value;
	private final int position;

	public ManifestString(final int position, final String value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(String.class)) {
			return this;
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	public String getValue() {
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

	@Override
	public String toString() {
		return "{ManifestString \"" + value + "\"}";
	}
}
