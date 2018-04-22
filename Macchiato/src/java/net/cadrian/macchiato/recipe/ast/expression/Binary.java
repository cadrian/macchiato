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
package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;

public abstract class Binary implements Expression {

	public static enum Operator {
		AND,
		OR,
		XOR,
		LT,
		LE,
		EQ,
		NE,
		GE,
		GT,
		MATCH,
		ADD,
		SUBTRACT,
		MULTIPLY,
		DIVIDE,
		REMAINDER,
		POWER;
	}

	private final Operator operator;

	protected Binary(final Operator operator) {
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}

}
