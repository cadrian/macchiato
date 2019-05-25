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

import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class MacMethod<T extends MacObject> extends MacCallable {

	private final Method<T> method;
	private final T target;

	public MacMethod(final Method<T> method, final T target) {
		this.method = method;
		this.target = target;
	}

	@Override
	public void invoke(final Context context, final int position) {
		method.run(target, context, position);
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return method.getArgTypes();
	}

	@Override
	public Identifier[] getArgNames() {
		return method.getArgNames();
	}

}
