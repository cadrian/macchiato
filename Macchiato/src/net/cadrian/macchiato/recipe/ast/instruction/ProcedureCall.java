package net.cadrian.macchiato.recipe.ast.instruction;

import net.cadrian.macchiato.recipe.ast.AbstractCall;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;

public class ProcedureCall extends AbstractCall implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitProcedureCall(ProcedureCall procedureCall);
	}

	public ProcedureCall(final int position, final String name) {
		super(position, name);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitProcedureCall(this);
	}

}
