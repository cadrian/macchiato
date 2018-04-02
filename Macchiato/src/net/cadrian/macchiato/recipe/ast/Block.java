package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public class Block implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visit(Block block);
	}

	private final int position;
	private final List<Instruction> instructions = new ArrayList<>();

	public Block(final int position) {
		this.position = position;
	}

	public void add(final Instruction instruction) {
		instructions.add(instruction);
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
