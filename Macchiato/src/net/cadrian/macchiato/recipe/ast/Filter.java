package net.cadrian.macchiato.recipe.ast;

public abstract class Filter implements Node {

	private final Block instructions;

	public Filter(final Block instructions) {
		this.instructions = instructions;
	}

	public Block getInstructions() {
		return instructions;
	}

}
