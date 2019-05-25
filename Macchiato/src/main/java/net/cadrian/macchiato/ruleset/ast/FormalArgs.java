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

public class FormalArgs {

	private final List<Identifier> args = new ArrayList<>();

	public void add(final Identifier arg) {
		args.add(arg);
	}

	public int size() {
		return args.size();
	}

	public Identifier get(final int i) {
		return args.get(i);
	}

	public Identifier[] toArray() {
		return args.toArray(new Identifier[args.size()]);
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		for (final Identifier arg : args) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(arg.getName());
		}
		return "(" + result + ")";
	}

}
