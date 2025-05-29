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
package net.cadrian.macchiato.ruleset.parser;

import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = -2289057144490764020L;

	public static final Position NONE = new Position(null, 0);

	public final String path;
	public final int offset;

	Position(final String path, final int position) {
		this.path = path;
		offset = position;
	}

	@Override
	public String toString() {
		return "{Position " + path + "@" + offset + "}";
	}

}
