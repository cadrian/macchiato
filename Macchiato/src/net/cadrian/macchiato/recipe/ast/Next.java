package net.cadrian.macchiato.recipe.ast;

public class Next implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visit(Next next);
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
		((Visitor) v).visit(this);
	}

}
