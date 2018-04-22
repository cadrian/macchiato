package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class CheckedExpression implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitCheckedExpression(CheckedExpression e);
	}

	private final Class<?> type;
	private final Expression toCheck;

	public CheckedExpression(final Expression toCheck, final Class<?> type) {
		this.toCheck = toCheck;
		this.type = type;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		return new CheckedExpression(toCheck, type);
	}

	@Override
	public int position() {
		return toCheck.position();
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	public Expression getToCheck() {
		return toCheck;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitCheckedExpression(this);
	}

	@Override
	public String toString() {
		return "{CheckedExpression " + type.getSimpleName() + ": " + toCheck + "}";
	}

}
