package net.cadrian.macchiato.ruleset.ast.instruction;

import net.cadrian.macchiato.ruleset.ast.Instruction;

public class DoNothing implements Instruction {

	public static final DoNothing instance = new DoNothing();

	private DoNothing() {
	}

	@Override
	public int position() {
		return 0;
	}

	@Override
	public Instruction simplify() {
		return this;
	}

	@Override
	public void accept(final Visitor v) {
	}

	@Override
	public String toString() {
		return "{DoNothing}";
	}

}
