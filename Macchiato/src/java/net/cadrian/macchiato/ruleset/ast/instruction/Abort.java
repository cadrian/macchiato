package net.cadrian.macchiato.ruleset.ast.instruction;

import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;

public class Abort implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitAbort(Abort abort);
	}

	private final int position;
	private final String message;

	public Abort(final int position, final String message) {
		this.position = position;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitAbort(this);
	}

	@Override
	public Instruction simplify() {
		return this;
	}

	@Override
	public String toString() {
		return "{Abort: " + message + "}";
	}

}
