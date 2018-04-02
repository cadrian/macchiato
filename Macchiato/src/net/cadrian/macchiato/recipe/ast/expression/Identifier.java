package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class Identifier implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visit(Identifier identifier);
	}

	private final String name;
	private final int position;

	public Identifier(final int position, final String name) {
		this.name = name;
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
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
		((Visitor) v).visit(this);
	}

}
