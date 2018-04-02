package net.cadrian.macchiato.recipe.ast;

public class While implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visit(While w);
	}

	private final int position;
	private final Expression condition;
	private final Instruction instruction;
	private final Instruction otherwise;

	public While(final int position, final Expression condition, final Instruction instruction,
			final Instruction otherwise) {
		this.position = position;
		this.condition = condition;
		this.instruction = instruction;
		this.otherwise = otherwise;
	}

	public Expression getCondition() {
		return condition;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public Instruction getOtherwise() {
		return otherwise;
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
