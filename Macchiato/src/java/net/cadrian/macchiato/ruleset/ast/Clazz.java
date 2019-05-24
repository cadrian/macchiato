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

import java.util.HashMap;
import java.util.Map;

import net.cadrian.macchiato.ruleset.Inheritance;

public class Clazz implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Clazz def);
	}

	private final String name;
	private final Expression invariant;
	private final Inheritance inheritance;
	private final Map<String, Def> defs = new HashMap<>();
	private final int position;

	public Clazz(final int position, final String name, final Inheritance inheritance, final Expression invariant) {
		this.position = position;
		this.name = name;
		this.invariant = invariant;
		this.inheritance = inheritance;
	}

	public String name() {
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

	public Def getDef(final String name) {
		return defs.get(name);
	}

	@Override
	public int position() {
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
