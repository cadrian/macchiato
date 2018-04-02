package net.cadrian.macchiato.recipe.ast;

public class Assignment implements Instruction {

	private final Expression leftSide;
	private final Expression rightSide;

	public Assignment(Expression leftSide, Expression rightSide) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
	}

	public Expression getLeftSide() {
		return leftSide;
	}

	public Expression getRightSide() {
		return rightSide;
	}

	@Override
	public int position() {
		return leftSide.position();
	}

}
