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
package net.cadrian.macchiato.ruleset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cadrian.macchiato.ruleset.ast.Node;

public class Inheritance implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Inheritance inheritance);
	}

	public static class Parent implements Node {

		public static interface Visitor extends Node.Visitor {
			void visit(Parent inheritance);
		}

		public static class Adapt implements Node {

			public static interface Visitor extends Node.Visitor {
				void visit(Adapt inheritance);
			}

			private final Map<String, String> renames = new HashMap<>();
			private final int position;

			public Adapt(final int position) {
				this.position = position;
			}

			public void addRename(final String old, final String ren) {
				renames.put(old, ren);
			}

			@Override
			public int position() {
				return position;
			}

			@Override
			public void accept(final Node.Visitor v) {
				((Visitor) v).visit(this);
			}

			public String getRename(final String old) {
				final String result = renames.get(old);
				return result == null ? old : result;
			}

		}

		private final boolean priv;
		private final String[] name;
		private final Adapt adapt;
		private final int position;

		public Parent(final boolean priv, final String[] name, final Adapt adapt, final int position) {
			this.priv = priv;
			this.name = name;
			this.adapt = adapt;
			this.position = position;
		}

		public boolean isPrivate() {
			return priv;
		}

		public String[] getName() {
			return name;
		}

		public Adapt getAdapt() {
			return adapt;
		}

		@Override
		public int position() {
			return position;
		}

		@Override
		public void accept(final Node.Visitor v) {
			((Visitor) v).visit(this);
		}

	}

	private final List<Parent> parents = new ArrayList<>();
	private final int position;

	public Inheritance(final int position) {
		this.position = position;
	}

	public void addParent(final Parent parent) {
		parents.add(parent);
	}

	public List<Parent> getParents() {
		return parents;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
