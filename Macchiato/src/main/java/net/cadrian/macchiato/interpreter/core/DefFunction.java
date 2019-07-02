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

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedDef;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class DefFunction implements Function {

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] NO_ARG_TYPES = (Class<? extends MacObject>[]) new Class<?>[0];
	private static final Identifier[] NO_ARG_NAMES = new Identifier[0];

	private final LocalizedDef def;
	private final Class<? extends MacObject>[] argTypes;
	private final Identifier[] argNames;

	@SuppressWarnings("unchecked")
	public DefFunction(final LocalizedDef def) {
		this.def = def;
		if (def.def != null) {
			final FormalArgs args = def.def.getArgs();
			argTypes = new Class[args.size()];
			Arrays.fill(argTypes, MacObject.class);
			argNames = args.toArray();
		} else {
			assert def.clazz != null;
			argTypes = NO_ARG_TYPES;
			argNames = NO_ARG_NAMES;
		}
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
		if (def.clazz != null) {
			final Clazs clazs = context.getClazs(def);
			clazs.getConstructor().run(context, position);
		} else if (def.def == null) {
			throw new InterpreterException("No def!!");
		} else {
			try {
				context.evaluateOldData(def.def.getEnsures());
				context.checkContract(def.def.getRequires(), "Requires");
				context.eval(def.def.getInstruction());
				context.checkContract(def.def.getEnsures(), "Ensures");
			} catch (final InterpreterException e) {
				throw new InterpreterException(e.getMessage(), e, position);
			}
		}
	}

}
