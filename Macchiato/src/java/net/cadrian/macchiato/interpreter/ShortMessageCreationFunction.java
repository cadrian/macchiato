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

import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class ShortMessageCreationFunction implements Function {

	private final ShortMessageType type;
	private final Ruleset ruleset;

	public ShortMessageCreationFunction(final ShortMessageType type, final Ruleset ruleset) {
		this.type = type;
		this.ruleset = ruleset;
	}

	@Override
	public String name() {
		return type.name();
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Class<?>[] getArgTypes() {
		return type.getArgTypes();
	}

	@Override
	public String[] getArgNames() {
		return type.getArgNames();
	}

	@Override
	public Class<?> getResultType() {
		return ShortMessage.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final String[] argNames = getArgNames();
		final Object[] args = new Object[argNames.length];
		for (int i = 0; i < argNames.length; i++) {
			args[i] = context.get(argNames[i]);
		}
		context.set("result", type.create(args));
	}

}
