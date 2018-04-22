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
import net.cadrian.macchiato.recipe.ast.Node;

public class CheckedExpression implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitCheckedExpression(CheckedExpression e);
	}

	private final Class<?> type;
	private final Expression toCheck;

	public CheckedExpression(final Expression toCheck, final Class<?> type) {
		this.toCheck = toCheck;
		this.type = type;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		return new CheckedExpression(toCheck, type);
	}

	@Override
	public int position() {
		return toCheck.position();
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	public Expression getToCheck() {
		return toCheck;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitCheckedExpression(this);
	}

	@Override
	public String toString() {
		return "{CheckedExpression " + type.getSimpleName() + ": " + toCheck + "}";
	}

}
