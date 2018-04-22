package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class Result implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visitResult(Result result);
	}

	private final int position;

	public Result(final int position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitResult(this);
	}

	@Override
	public String toString() {
		return "{Result}";
	}

}
