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
package net.cadrian.macchiato.interpreter.objects;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacClazsObject implements MacObject {

	private final Clazs clazs;

	public MacClazsObject(final Clazs clazs) {
		this.clazs = clazs;
	}

	public Clazs getClazs() {
		return clazs;
	}

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset, final Identifier name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		// TODO Auto-generated method stub
		return null;
	}

}
