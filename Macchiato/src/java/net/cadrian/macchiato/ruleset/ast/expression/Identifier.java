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

import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;

public class Identifier implements Expression, Comparable<Identifier> {

	public static interface Visitor extends Node.Visitor {
		void visitIdentifier(Identifier identifier);
	}

	private final String name;
	private final int position;

	public Identifier(final String name, final int position) {
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("null identifier");
		}
		this.name = name;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		return new CheckedExpression(this, type);
	}

	public boolean isPublic() {
		return Character.isUpperCase(name.charAt(0));
	}

	public String getName() {
		return name;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitIdentifier(this);
	}

	@Override
	public Identifier simplify() {
		return this;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public Expression getStaticValue() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Identifier))
			return false;
		return compareTo((Identifier) obj) == 0;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(Identifier other) {
		return name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return "{Identifier:" + name + "}";
	}

}
