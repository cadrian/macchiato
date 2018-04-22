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

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;

public class While implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitWhile(While w);
	}

	private final int position;
	private final Expression condition;
	private final Instruction instruction;
	private final Instruction otherwise;

	public While(final int position, final Expression condition, final Instruction instruction,
			final Instruction otherwise) {
		this.position = position;
		this.condition = condition;
		this.instruction = instruction;
		this.otherwise = otherwise;
	}

	public Expression getCondition() {
		return condition;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public Instruction getOtherwise() {
		return otherwise;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitWhile(this);
	}

	@Override
	public String toString() {
		return "{While " + condition + ": " + instruction + " Else " + otherwise + "}";
	}

}