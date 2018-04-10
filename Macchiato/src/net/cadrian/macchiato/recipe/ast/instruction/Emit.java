package net.cadrian.macchiato.recipe.ast.instruction;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;

public class Emit implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitEmit(Emit emit);
	}

	private final int position;
	private final TypedExpression message;
	private final TypedExpression tick;

	public Emit(final int position, final TypedExpression message, final TypedExpression tick) {
		assert message.getType() == Message.class;
		assert message != null || tick == null;
		this.position = position;
		this.message = message;
		this.tick = tick;
	}

	@Override
	public int position() {
		return position;
	}

	public TypedExpression getMessage() {
		return message;
	}

	public TypedExpression getTick() {
		return tick;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitEmit(this);
	}

	@Override
	public String toString() {
		if (message == null) {
			return "{Emit}";
		}
		if (tick == null) {
			return "{Emit " + message + "}";
		}
		return "{Emit " + message + " at " + tick + "}";
	}

}
