package net.cadrian.macchiato.recipe.ast;

public class Assignment implements Instruction {

	private final int position;
	private final String name;
	private final Expression expression;

	public Assignment(final int position, final String name, final Expression expression) {
		this.position = position;
		this.name = name;
		this.expression = expression;
	}

	public String getName() {
		return name;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public int position() {
		return position;
	}

}
