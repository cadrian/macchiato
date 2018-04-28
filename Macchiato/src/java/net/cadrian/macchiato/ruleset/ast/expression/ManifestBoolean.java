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

import net.cadrian.macchiato.ruleset.ast.Node;

public class ManifestBoolean implements TypedExpression {

	public static interface Visitor extends Node.Visitor {
		void visitManifestBoolean(ManifestBoolean manifestBoolean);
	}

	private final boolean value;
	private final int position;

	public ManifestBoolean(final int position, final boolean value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		if (type.isAssignableFrom(Boolean.class)) {
			return this;
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitManifestBoolean(this);
	}

	@Override
	public String toString() {
		return "{ManifestBoolean " + value + "}";
	}

}
