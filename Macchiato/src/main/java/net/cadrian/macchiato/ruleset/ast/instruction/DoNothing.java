package net.cadrian.macchiato.ruleset.ast.instruction;

import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.parser.Position;

public final class DoNothing implements Instruction {

	public static final DoNothing INSTANCE = new DoNothing();

	private DoNothing() {
		throw new IllegalStateException("unexpected call");
	}

	@Override
	public Position position() {
		return Position.NONE;
	}

	@Override
	public Instruction simplify() {
		return this;
	}

	@Override
	public void accept(final Visitor v) {
		// do nothing :-)
	}

	@Override
	public String toString() {
		return "{DoNothing}";
	}

}
