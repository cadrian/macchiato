package net.cadrian.macchiato.recipe.ast.instruction;

import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;

public class Next implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitNext(Next next);
	}

	private final int position;

	public Next(final int position) {
		this.position = position;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitNext(this);
	}

	@Override
	public String toString() {
		return "{Next}";
	}

}
