package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.List;

public class Block implements Instruction {

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

}
