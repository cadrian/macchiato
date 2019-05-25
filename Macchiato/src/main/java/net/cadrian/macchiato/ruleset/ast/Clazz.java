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
package net.cadrian.macchiato.ruleset.ast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Clazz implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Clazz def);
	}

	private final Identifier name;
	private final Expression invariant;
	private final Inheritance inheritance;
	private final Map<Identifier, Def> defs = new HashMap<>();
	private final Map<Identifier, Identifier> fields = new HashMap<>();
	private final Position position;

	public Clazz(final Position position, final Identifier name, final Inheritance inheritance,
			final Expression invariant) {
		this.position = position;
		this.name = name;
		this.invariant = invariant;
		this.inheritance = inheritance;
	}

	public Identifier name() {
		return name;
	}

	public Expression getInvariant() {
		return invariant;
	}

	public Inheritance getInheritance() {
		return inheritance;
	}

	public Def addDef(final Def def) {
		return defs.put(def.name(), def);
	}

	public Def getDef(final Identifier name) {
		return defs.get(name);
	}

	public Identifier addField(final Identifier field) {
		return fields.put(field, field);
	}

	public Collection<Def> getDefs() {
		return defs.values();
	}

	public Collection<Identifier> getFields() {
		return fields.values();
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

	public Clazz simplify() {
		return this;
	}

	@Override
	public String toString() {
		return "{Class: " + name + "}";
	}

}
