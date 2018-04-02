package net.cadrian.macchiato.recipe.ast;

public class Assignment implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visit(Assignment assignment);
	}

	private final Expression leftSide;
	private final Expression rightSide;

	public Assignment(final Expression leftSide, final Expression rightSide) {
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

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
