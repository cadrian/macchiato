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

import java.util.Arrays;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

class ClazzClazsMethod implements ClazsMethod {

	private final ClazzClazs clazzClazs;
	private final Def def;
	private final Identifier name;
	private final Ruleset ruleset;
	private final Class<? extends MacObject>[] argTypes;
	private final Identifier[] argNames;

	@SuppressWarnings("unchecked")
	ClazzClazsMethod(final ClazzClazs clazzClazs, final Def def, final Identifier name, final Ruleset ruleset) {
		this.clazzClazs = clazzClazs;
		this.def = def;
		this.name = name;
		this.ruleset = ruleset;

		final FormalArgs args = def.getArgs();
		argTypes = new Class[args.size()];
		Arrays.fill(argTypes, MacObject.class);
		argNames = args.toArray();
	}

	@Override
	public Identifier name() {
		return name;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return argTypes;
	}

	@Override
	public Identifier[] getArgNames() {
		return argNames;
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
	public Class<MacClazsObject> getTargetType() {
		return MacClazsObject.class;
	}

	@Override
	public Clazs getTargetClazs() {
		return clazzClazs;
	}

	@Override
	public void run(final MacClazsObject target, final Context context, final int position) {
		final ClazsContext clazsContext;
		if (context instanceof ClazsContext && ((ClazsContext) context).getTarget() == target) {
			clazsContext = (ClazsContext) context;
		} else {
			clazsContext = new ClazsContext(context, target, ruleset);
		}
		try {
			clazsContext.eval(def.getInstruction());
		} catch (final InterpreterException e) {
			throw new InterpreterException(e.getMessage(), e, position);
		}
	}

}
