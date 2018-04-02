package net.cadrian.macchiato.recipe.ast;

import net.cadrian.macchiato.midi.Event;

public class Emit implements Instruction {

	private final int position;
	private final TypedExpression<Event> expression;

	public Emit(final int position, final TypedExpression<Event> expression) {
		this.position = position;
		this.expression = expression;
	}

	@Override
	public int position() {
		return position;
	}

}
