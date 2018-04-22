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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.cadrian.macchiato.interpreter.Array;
import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestArray implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitManifestArray(ManifestArray manifestArray);
	}

	private final List<Expression> expressions = new ArrayList<>();
	private final int position;

	public ManifestArray(final int position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(ManifestArray.class)) {
			return this;
		}
		return new CheckedExpression(this, type);
	}

	@Override
	public Class<?> getType() {
		return Array.class;
	}

	@Override
	public int position() {
		return position;
	}

	public void add(final Expression expression) {
		expressions.add(expression);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitManifestArray(this);
	}

	public List<Expression> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

	@Override
	public String toString() {
		return "{ManifestArray " + expressions + "}";
	}

}
