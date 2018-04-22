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