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
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class ManifestDictionary implements ManifestExpression<Void> {

	private final List<Entry> expressions = new ArrayList<>();
	private final Position position;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
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

	public ManifestDictionary(final Position position) {
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		if (type.isAssignableFrom(ManifestDictionary.class)) {
			return this;
		}
		return new CheckedExpression(this, type);
	}

	@Override
	public Class<? extends MacObject> getType() {
		return MacDictionary.class;
	}

	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public Position position() {
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

	Expression getExpression(final ManifestString index) {
		final String key = index.getValue();
		for (final Entry entry : expressions) {
			final ManifestString entryKey = (ManifestString) entry.key.getStaticValue();
			if (entryKey.getValue().equals(key)) {
				return entry.expression;
			}
		}
		return null;
	}

	@Override
	public TypedExpression simplify() {
		boolean changed = false;
		final ManifestDictionary result = new ManifestDictionary(position);
		for (final Entry entry : expressions) {
			final TypedExpression key = entry.key;
			final TypedExpression simplifyKey = key.simplify();
			final Expression expression = entry.expression;
			final Expression simplifyExpression = expression.simplify();
			result.put(simplifyKey, simplifyExpression);
			changed |= !simplifyKey.equals(key) || !simplifyExpression.equals(expression);
		}
		return changed ? result : this;
	}

	@Override
	public boolean isStatic() {
		for (final Entry entry : expressions) {
			if (!entry.getKey().isStatic()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Expression getStaticValue() {
		if (isStatic()) {
			return this;
		}
		return null;
	}

	@Override
	public String toString() {
		return "{ManifestDictionary " + expressions + "}";
	}

}
