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
package net.cadrian.macchiato.interpreter.core.clazs;

import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

class MethodFunction implements Function {

	private final ClazsMethod method;

	MethodFunction(final ClazsMethod method) {
		this.method = method;
	}

	@Override
	public Identifier name() {
		return method.name();
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return method.getArgTypes();
	}

	@Override
	public Identifier[] getArgNames() {
		return method.getArgNames();
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		return method.getResultType();
	}

	@Override
	public Ruleset getRuleset() {
		return method.getRuleset();
	}

	@Override
	public void run(final Context context, final Position position) {
		method.run(((ClazsContext) context).getTarget(), context, position);
	}

}
