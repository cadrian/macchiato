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

import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;

public class Assignment implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitAssignment(Assignment assignment);
	}

	private final Expression leftSide;
	private final Expression rightSide;

	public Assignment(final Expression leftSide, final Expression rightSide) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
	}

	public Expression getLeftSide() {
		return leftSide;
	}

	public Expression getRightSide() {
		return rightSide;
	}

	@Override
	public int position() {
		return leftSide.position();
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitAssignment(this);
	}

	@Override
	public Instruction simplify() {
		final Expression left = leftSide.simplify();
		final Expression right = rightSide.simplify();
		if (left == leftSide && right == rightSide) {
			return this;
		}
		return new Assignment(left, right);
	}

	@Override
	public String toString() {
		return "{Assignment " + leftSide + " = " + rightSide + "}";
	}

}
