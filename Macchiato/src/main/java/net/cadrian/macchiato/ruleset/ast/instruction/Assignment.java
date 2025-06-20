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
import net.cadrian.macchiato.ruleset.parser.Position;

public class Assignment implements Instruction {

	private final Expression leftSide;
	private final Expression rightSide;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitAssignment(Assignment assignment);
	}

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
	public Position position() {
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
		if (left.equals(leftSide) && right.equals(rightSide)) {
			return this;
		}
		return new Assignment(left, right);
	}

	@Override
	public String toString() {
		return "{Assignment " + leftSide + " = " + rightSide + "}";
	}

}
