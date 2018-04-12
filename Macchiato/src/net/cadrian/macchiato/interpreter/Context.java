package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;

abstract class Context {

	abstract Interpreter getInterpreter();

	abstract Track getTrack();

	abstract AbstractEvent getEvent();

	void emit() {
		final AbstractEvent event = getEvent();
		emit(event.createMessage(), event.getTick());
	}

	abstract void emit(Message message, BigInteger tick);

	abstract boolean isNext();

	abstract void setNext(boolean next);

	void eval(final Instruction instruction) {
		instruction.accept(new InstructionEvaluationVisitor(this));
	}

	Object eval(final TypedExpression expression) {
		final ExpressionEvaluationVisitor v = new ExpressionEvaluationVisitor(this, expression.getType());
		expression.accept(v);
		return v.getLastValue();
	}
	
	abstract Function getFunction(String name);

	abstract <T> T get(String key);

	abstract <T> T set(String key, T value);

	abstract <T> T setGlobal(String key, T value);

}
