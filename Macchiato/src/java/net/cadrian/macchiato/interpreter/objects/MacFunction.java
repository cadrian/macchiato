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

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacFunction extends MacCallable {

	private final Function function;

	public MacFunction(final Function function) {
		this.function = function;
	}

	@Override
	public void invoke(final Context context, final int position) {
		function.run(context, position);
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return function.getArgTypes();
	}

	@Override
	public Identifier[] getArgNames() {
		return function.getArgNames();
	}

}
