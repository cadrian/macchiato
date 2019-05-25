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
import net.cadrian.macchiato.ruleset.parser.Position;

public class Identifier implements Expression, Comparable<Identifier> {

	public static interface Visitor extends Node.Visitor {
		void visitIdentifier(Identifier identifier);
	}

	private final String name;
	private final Position position;

	public Identifier(final String name, final Position position) {
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("null identifier");
		}
		this.name = name;
		this.position = position == null ? Position.NONE : position;
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
	public Position position() {
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
	public boolean equals(final Object obj) {
		if (!(obj instanceof Identifier)) {
			return false;
		}
		return name.equals(((Identifier) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(final Identifier other) {
		return name.compareTo(other.name);
	}

	@Override
	public String toString() {
		return "{Identifier:" + name + "}";
	}

}
