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
package net.cadrian.macchiato.interpreter;

import java.util.Arrays;

import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;

public class DefFunction implements Function {

	private final Def def;
	private final Class<?>[] argsType;
	private final String[] argNames;

	public DefFunction(final Def def) {
		this.def = def;
		final FormalArgs args = def.getArgs();
		argsType = new Class<?>[args.size()];
		Arrays.fill(argsType, Object.class);
		argNames = args.toArray();
	}

	@Override
	public String name() {
		return def.name();
	}

	@Override
	public Class<?>[] getArgTypes() {
		return argsType;
	}

	@Override
	public String[] getArgNames() {
		return argNames;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public void run(final Context context, final int position) {
		try {
			final InstructionEvaluationVisitor v = new InstructionEvaluationVisitor(context);
			def.getBlock().accept(v);
		} catch (final InterpreterException e) {
			throw new InterpreterException(e.getMessage(), position, e);
		}
	}

}
