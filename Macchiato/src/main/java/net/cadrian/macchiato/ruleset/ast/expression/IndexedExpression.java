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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class IndexedExpression implements Expression {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexedExpression.class);

	private final Expression indexed;
	private final TypedExpression index;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitIndexedExpression(IndexedExpression indexedExpression);
	}

	public IndexedExpression(final Expression indexed, final TypedExpression index) {
		this.indexed = indexed;
		this.index = index;
	}

	@Override
	public Position position() {
		return indexed.position();
	}

	public TypedExpression getIndex() {
		return index;
	}

	public Expression getIndexed() {
		return indexed;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitIndexedExpression(this);
	}

	@Override
	public Expression simplify() {
		final Expression simplifyIndexed = indexed.simplify();
		final TypedExpression simplifyIndex = index.simplify();
		if (simplifyIndexed.equals(indexed) && simplifyIndex.equals(index)) {
			return this;
		}
		if (simplifyIndexed.isStatic() && simplifyIndex.isStatic()) {
			LOGGER.debug("static index, should be simplified"); // TODO
		}
		return new IndexedExpression(simplifyIndexed, simplifyIndex);
	}

	@Override
	public boolean isStatic() {
		return indexed.isStatic() && index.isStatic();
	}

	@Override
	public Expression getStaticValue() {
		if (index.getType() == MacNumber.class) {
			LOGGER.debug("replace indexed array by the actual element");
			final ManifestArray array = (ManifestArray) indexed.getStaticValue();
			final ManifestNumeric key = (ManifestNumeric) index.getStaticValue();
			return array.getExpression(key);
		}
		if (index.getType() == MacString.class) {
			LOGGER.debug("replace indexed dictionary by the actual element");
			final ManifestDictionary dictionary = (ManifestDictionary) indexed.getStaticValue();
			final ManifestString key = (ManifestString) index.getStaticValue();
			return dictionary.getExpression(key);
		}
		return null;
	}

	@Override
	public String toString() {
		return "{IndexedExpression " + indexed + " at " + index + "}";
	}

}
