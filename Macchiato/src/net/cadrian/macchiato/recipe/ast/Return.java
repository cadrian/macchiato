package net.cadrian.macchiato.recipe.ast;

public class Return implements Instruction {

	private final Expression expression;
	private final int position;

	public Return(final int position) {
		this(position, null);
	}

	public Return(final int position, final Expression expression) {
		this.position = position;
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public int position() {
		return position;
	}

}
