package net.cadrian.macchiato.recipe.ast;

public class Def implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Def def);
	}

	private final String name;
	private final FormalArgs args;
	private final Instruction instruction;
	private final int position;

	public Def(final int position, final String name, final FormalArgs args, final Instruction instruction) {
		this.position = position;
		this.name = name;
		this.args = args;
		this.instruction = instruction;
	}

	public String name() {
		return name;
	}

	public FormalArgs getArgs() {
		return args;
	}

	public Instruction getInstruction() {
		return instruction;
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
