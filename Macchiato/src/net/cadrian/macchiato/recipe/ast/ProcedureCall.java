package net.cadrian.macchiato.recipe.ast;

public class ProcedureCall extends AbstractCall implements Instruction {

	public ProcedureCall(final int position, final String name) {
		super(position, name);
	}

}
