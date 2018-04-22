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

import net.cadrian.macchiato.recipe.ast.Node;

public class ManifestString implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitManifestString(ManifestString manifestString);
	}

	private final String value;
	private final int position;

	public ManifestString(final int position, final String value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(String.class)) {
			return this;
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitManifestString(this);
	}

	@Override
	public String toString() {
		return "{ManifestString \"" + value + "\"}";
	}
}
