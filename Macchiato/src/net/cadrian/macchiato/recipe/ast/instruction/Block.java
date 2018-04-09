package net.cadrian.macchiato.recipe.ast.instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;

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

	public List<Instruction> getInstructions() {
		return Collections.unmodifiableList(instructions);
	}

	@Override
	public String toString() {
		return "{Block " + instructions + "}";
	}

}
