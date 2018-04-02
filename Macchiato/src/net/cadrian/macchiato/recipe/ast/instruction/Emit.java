package net.cadrian.macchiato.recipe.ast.instruction;

import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;
import net.cadrian.macchiato.recipe.interpreter.AbstractEvent;

public class Emit implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitEmit(Emit emit);
	}

	private final int position;
	private final TypedExpression<AbstractEvent> expression;

	public Emit(final int position, final TypedExpression<AbstractEvent> expression) {
		this.position = position;
		this.expression = expression;
	}

	@Override
	public int position() {
		return position;
	}

	public TypedExpression<AbstractEvent> getExpression() {
		return expression;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitEmit(this);
	}

}
