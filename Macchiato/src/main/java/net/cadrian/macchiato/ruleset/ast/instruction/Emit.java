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
package net.cadrian.macchiato.ruleset.ast.instruction;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Emit implements Instruction {

	private final Position position;
	private final TypedExpression message;
	private final TypedExpression tick;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitEmit(Emit emit);
	}

	public Emit(final Position position, final TypedExpression message, final TypedExpression tick) {
		if (message != null && message.getType() != Message.class) {
			throw new IllegalArgumentException("invalid message");
		}
		if (message == null && tick != null) {
			throw new IllegalArgumentException("invalid tick");
		}
		this.position = position;
		this.message = message;
		this.tick = tick;
	}

	@Override
	public Position position() {
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
	public Instruction simplify() {
		final TypedExpression simplifyMessage = message == null ? null : message.simplify();
		final TypedExpression simplifyTick = tick == null ? null : tick.simplify();
		if (message.equals(simplifyMessage) && tick.equals(simplifyTick)) {
			return this;
		}
		return new Emit(position, simplifyMessage, simplifyTick);
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
