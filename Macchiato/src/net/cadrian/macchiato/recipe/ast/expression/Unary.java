package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;

public abstract class Unary implements Expression {

	public static enum Operator {
		NOT,
		MINUS;
	}

	private final Operator operator;

	protected Unary(final Operator operator) {
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}

}
