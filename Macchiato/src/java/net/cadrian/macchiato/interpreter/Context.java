/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;

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
