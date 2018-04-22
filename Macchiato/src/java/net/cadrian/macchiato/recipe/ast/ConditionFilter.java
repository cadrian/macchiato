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
package net.cadrian.macchiato.recipe.ast;

import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;
import net.cadrian.macchiato.recipe.ast.instruction.Block;

public class ConditionFilter extends Filter {

	public static interface Visitor extends Node.Visitor {
		void visit(ConditionFilter conditionFilter);
	}

	private final TypedExpression condition;

	public ConditionFilter(final TypedExpression condition, final Block instructions) {
		super(instructions);
		assert (condition.getType() == Boolean.class);
		this.condition = condition;
	}

	public TypedExpression getCondition() {
		return condition;
	}

	@Override
	public int position() {
		return condition.position();
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
