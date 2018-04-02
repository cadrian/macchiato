package net.cadrian.macchiato.recipe.ast;

public class Next implements Instruction {

	private final int position;

	public Next(final int position) {
		this.position = position;
	}

	@Override
	public int position() {
		return position;
	}

}
