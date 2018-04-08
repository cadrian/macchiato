package net.cadrian.macchiato.recipe.interpreter;

import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;

abstract class Context {

	abstract Interpreter getInterpreter();

	abstract Track getTrack();

	abstract AbstractEvent getEvent();

	void emit() {
		emit(getEvent());
	}

	abstract void emit(AbstractEvent event);

	abstract boolean isNext();

	abstract void setNext(boolean next);

	void eval(final Instruction instruction) {
		instruction.accept(new InstructionEvaluationVisitor(this));
	}

	Object eval(final TypedExpression expression) {
		final ExpressionEvaluationVisitor v = new ExpressionEvaluationVisitor(this, expression.getType());
		expression.accept(v);
		return v.getResult();
	}

	abstract <T> T get(String key);

	abstract <T> T set(String key, T value);

}
