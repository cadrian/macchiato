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

import java.util.ArrayList;
import java.util.List;

import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Inheritance implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Inheritance inheritance);
	}

	public static class Parent implements Node {

		public static interface Visitor extends Node.Visitor {
			void visit(Parent inheritance);
		}

		private final Identifier[] name;
		private final Position position;

		public Parent(final Identifier[] name, final Position position) {
			this.name = name;
			this.position = position;
		}

		public Identifier[] getName() {
			return name;
		}

		@Override
		public Position position() {
			return position;
		}

		@Override
		public void accept(final Node.Visitor v) {
			((Visitor) v).visit(this);
		}

	}

	private final List<Parent> parents = new ArrayList<>();
	private final Position position;

	public Inheritance(final Position position) {
		this.position = position;
	}

	public void addParent(final Parent parent) {
		parents.add(parent);
	}

	public List<Parent> getParents() {
		return parents;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
