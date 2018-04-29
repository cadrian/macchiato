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
import java.util.Collections;
import java.util.List;

public abstract class AbstractCall {

	protected final String name;
	protected final int position;
	protected final List<Expression> arguments = new ArrayList<>();

	public AbstractCall(final int position, final String name) {
		this.name = name;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void add(final Expression exp) {
		arguments.add(exp);
	}

	public int position() {
		return position;
	}

	public List<Expression> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	@Override
	public String toString() {
		return "{" + getClass().getSimpleName() + " " + name + arguments + "}";
	}

}
