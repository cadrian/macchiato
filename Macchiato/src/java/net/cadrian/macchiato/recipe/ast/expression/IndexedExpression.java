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

public class IndexedExpression implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visitIndexedExpression(IndexedExpression indexedExpression);
	}

	private final Expression indexed;
	private final TypedExpression index;

	public IndexedExpression(final Expression indexed, final TypedExpression index) {
		this.indexed = indexed;
		this.index = index;
	}

	@Override
	public int position() {
		return indexed.position();
	}

	public TypedExpression getIndex() {
		return index;
	}

	public Expression getIndexed() {
		return indexed;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitIndexedExpression(this);
	}

	@Override
	public String toString() {
		return "{IndexedExpression " + indexed + " at " + index + "}";
	}

}
