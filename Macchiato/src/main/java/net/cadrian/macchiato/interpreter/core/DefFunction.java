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
package net.cadrian.macchiato.interpreter.core;

import java.util.Arrays;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedDef;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class DefFunction implements Function {

	private final LocalizedDef def;
	private final Class<? extends MacObject>[] argTypes;
	private final Identifier[] argNames;

	@SuppressWarnings("unchecked")
	public DefFunction(final LocalizedDef def) {
		this.def = def;
		final FormalArgs args = def.def.getArgs();
		argTypes = new Class[args.size()];
		Arrays.fill(argTypes, MacObject.class);
		argNames = args.toArray();
	}

	@Override
	public Identifier name() {
		return def.def.name();
	}

	@Override
	public Ruleset getRuleset() {
		return def.ruleset;
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
	public void run(final Context context, final Position position) {
		try {
			context.eval(def.def.getInstruction());
		} catch (final InterpreterException e) {
			throw new InterpreterException(e.getMessage(), e, position);
		}
	}

}
