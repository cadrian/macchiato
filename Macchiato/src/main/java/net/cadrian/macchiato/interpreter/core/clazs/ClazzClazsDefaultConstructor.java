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

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsConstructor;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

class ClazzClazsDefaultConstructor implements ClazsConstructor {

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] ARG_TYPES = (Class<? extends MacObject>[]) new Class<?>[0];
	private static final Identifier[] ARG_NAMES = {};

	private final ClazzClazs clazzClazs;
	private final Identifier name;
	private final Ruleset ruleset;

	ClazzClazsDefaultConstructor(final ClazzClazs clazzClazs, final Identifier name, final Ruleset ruleset) {
		this.clazzClazs = clazzClazs;
		this.name = name;
		this.ruleset = ruleset;
	}

	@Override
	public Identifier name() {
		return name;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return ARG_TYPES;
	}

	@Override
	public Identifier[] getArgNames() {
		return ARG_NAMES;
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		return MacObject.class;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Clazs getTargetClazs() {
		return clazzClazs;
	}

	@Override
	public void run(final Context context, final Position position) {
		context.set(Identifiers.RESULT, new MacClazsObject(clazzClazs));
	}

}
