package net.cadrian.macchiato.recipe.ast.instruction;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;

public class Assignment implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitAssignment(Assignment assignment);
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
		((Visitor) v).visitAssignment(this);
	}

	@Override
	public String toString() {
		return "{Assignment " + leftSide + " = " + rightSide + "}";
	}

}
