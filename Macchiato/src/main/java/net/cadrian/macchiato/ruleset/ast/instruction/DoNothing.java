package net.cadrian.macchiato.ruleset.ast.instruction;

import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.parser.Position;

public class DoNothing implements Instruction {

	public static final DoNothing instance = new DoNothing();

	private DoNothing() {
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
