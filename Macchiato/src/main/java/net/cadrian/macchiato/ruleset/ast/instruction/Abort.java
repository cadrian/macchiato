package net.cadrian.macchiato.ruleset.ast.instruction;

import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Abort implements Instruction {

	private final Position position;
	private final String message;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitAbort(Abort abort);
	}

	public Abort(final Position position, final String message) {
		this.position = position;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public Position position() {
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
