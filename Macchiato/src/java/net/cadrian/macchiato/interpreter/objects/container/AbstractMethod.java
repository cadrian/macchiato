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
package net.cadrian.macchiato.interpreter.objects.container;

import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public abstract class AbstractMethod<T extends MacObject> implements Method<T> {

	protected final Ruleset ruleset;

	AbstractMethod(final Ruleset ruleset) {
		this.ruleset = ruleset;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public String toString() {
		return "Method: " + name();
	}

}
