package net.cadrian.macchiato.recipe.ast;

public abstract class Binary implements Expression {

	public static enum Operator {
		AND,
		OR,
		XOR,
		LT,
		LE,
		EQ,
		NE,
		GE,
		GT,
		MATCH,
		ADD,
		SUBTRACT,
		MULTIPLY,
		DIVIDE,
		REMAINDER,
		POWER;
	}

	private final Operator operator;

	protected Binary(final Operator operator) {
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}

}
