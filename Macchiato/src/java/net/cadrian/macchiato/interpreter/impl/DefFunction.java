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
package net.cadrian.macchiato.interpreter.impl;

import java.util.Arrays;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedDef;

public class DefFunction implements Function {

	private final LocalizedDef def;
	private final Class<? extends MacObject>[] argsType;
	private final String[] argNames;

	@SuppressWarnings("unchecked")
	public DefFunction(final LocalizedDef def) {
		this.def = def;
		final FormalArgs args = def.def.getArgs();
		argsType = new Class[args.size()];
		Arrays.fill(argsType, MacObject.class);
		argNames = args.toArray();
	}

	@Override
	public String name() {
		return def.def.name();
	}

	@Override
	public Ruleset getRuleset() {
		return def.ruleset;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return argsType;
	}

	@Override
	public String[] getArgNames() {
		return argNames;
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		return MacObject.class;
	}

	@Override
	public void run(final Context context, final int position) {
		try {
			final InstructionEvaluationVisitor v = new InstructionEvaluationVisitor(context);
			def.def.getInstruction().accept(v);
		} catch (final InterpreterException e) {
			throw new InterpreterException(e.getMessage(), position, e);
		}
	}

}