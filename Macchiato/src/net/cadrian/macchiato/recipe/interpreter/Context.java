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

	<T> T eval(final TypedExpression expression) {
		// TODO
		return null;
	}

	abstract <T> T get(String key);

	abstract <T> T put(String key, T value);

}
