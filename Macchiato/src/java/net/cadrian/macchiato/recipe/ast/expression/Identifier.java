package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class Identifier implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visitIdentifier(Identifier identifier);
	}

	private final String name;
	private final int position;

	public Identifier(final int position, final String name) {
		if (name == null) {
			throw new NullPointerException("null identifier");
		}
		this.name = name;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		return new CheckedExpression(this, type);
	}

	public String getName() {
		return name;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitIdentifier(this);
	}

	@Override
	public String toString() {
		return "{Identifier:" + name + "}";
	}

}
