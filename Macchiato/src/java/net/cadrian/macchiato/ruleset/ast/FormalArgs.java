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

public class FormalArgs {

	private final List<String> args = new ArrayList<>();

	public void add(final String arg) {
		args.add(arg);
	}

	public int size() {
		return args.size();
	}

	public String get(final int i) {
		return args.get(i);
	}

	public String[] toArray() {
		return args.toArray(new String[args.size()]);
	}

}
