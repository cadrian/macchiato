package net.cadrian.macchiato.recipe.ast;

public class ProcedureCall extends AbstractCall implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visit(ProcedureCall procedureCall);
	}

	public ProcedureCall(final int position, final String name) {
		super(position, name);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
