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
package net.cadrian.macchiato.ruleset.ast;

import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;

public class ConditionFilter extends Filter {

	public static interface Visitor extends Node.Visitor {
		void visit(ConditionFilter conditionFilter);
	}

	private final TypedExpression condition;
	private final int position;

	public ConditionFilter(final int position, final TypedExpression condition, final Instruction instruction) {
		super(instruction);
		assert (condition.getType() == Boolean.class);
		this.condition = condition;
		this.position = position;
	}

	public TypedExpression getCondition() {
		return condition;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

	@Override
	public Filter simplify() {
		final Instruction simplifyInstruction = instruction.simplify();
		if (simplifyInstruction == instruction) {
			return this;
		}
		return new ConditionFilter(position, condition.simplify(), simplifyInstruction);
	}

	@Override
	public String toString() {
		return "{Filter condition:" + condition + " " + instruction + "}";
	}

}
