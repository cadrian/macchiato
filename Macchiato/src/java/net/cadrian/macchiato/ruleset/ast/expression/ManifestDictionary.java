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

import net.cadrian.macchiato.interpreter.Dictionary;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;

public class ManifestDictionary implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitManifestDictionary(ManifestDictionary manifestDictionary);
	}

	public static class Entry {
		private final TypedExpression key;
		private final Expression expression;

		Entry(final TypedExpression key, final Expression expression) {
			this.key = key;
			this.expression = expression;
		}

		public TypedExpression getKey() {
			return key;
		}

		public Expression getExpression() {
			return expression;
		}
	}

	private final List<Entry> expressions = new ArrayList<>();
	private final int position;

	public ManifestDictionary(final int position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(ManifestDictionary.class)) {
			return this;
		}
		return new CheckedExpression(this, type);
	}

	@Override
	public Class<?> getType() {
		return Dictionary.class;
	}

	@Override
	public int position() {
		return position;
	}

	public void put(final TypedExpression key, final Expression expression) {
		expressions.add(new Entry(key, expression));
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitManifestDictionary(this);
	}

	public List<Entry> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

	@Override
	public String toString() {
		return "{ManifestDictionary " + expressions + "}";
	}

}
