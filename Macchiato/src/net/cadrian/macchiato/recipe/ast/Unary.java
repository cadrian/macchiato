package net.cadrian.macchiato.recipe.ast;

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
