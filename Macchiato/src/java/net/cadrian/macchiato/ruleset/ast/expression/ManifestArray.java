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
package net.cadrian.macchiato.ruleset.ast.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;

public class ManifestArray implements ManifestExpression<Void> {

	public static interface Visitor extends Node.Visitor {
		void visitManifestArray(ManifestArray manifestArray);
	}

	private final List<Expression> expressions = new ArrayList<>();
	private final int position;

	public ManifestArray(final int position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		if (type.isAssignableFrom(ManifestArray.class)) {
			return this;
		}
		return new CheckedExpression(this, type);
	}

	@Override
	public Class<? extends MacObject> getType() {
		return MacArray.class;
	}

	@Override
	public Void getValue() {
		return null;
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

	Expression getExpression(final ManifestNumeric index) {
		final int i = index.getValue().intValueExact();
		return expressions.get(i);
	}

	@Override
	public TypedExpression simplify() {
		boolean changed = false;
		final ManifestArray result = new ManifestArray(position);
		for (final Expression value : expressions) {
			final Expression simplifyValue = value.simplify();
			result.add(simplifyValue);
			changed |= simplifyValue != value;
		}
		return changed ? result : this;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public Expression getStaticValue() {
		return this;
	}

	@Override
	public String toString() {
		return "{ManifestArray " + expressions + "}";
	}

}
