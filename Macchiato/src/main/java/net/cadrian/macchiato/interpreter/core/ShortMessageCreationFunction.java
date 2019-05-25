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

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class ShortMessageCreationFunction implements Function {

	private final Identifier name;
	private final ShortMessageType type;
	private final Ruleset ruleset;

	public ShortMessageCreationFunction(final ShortMessageType type, final Ruleset ruleset) {
		this.name = new Identifier(type.name(), Position.NONE);
		this.type = type;
		this.ruleset = ruleset;
	}

	@Override
	public Identifier name() {
		return name;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		return type.getArgTypes();
	}

	@Override
	public Identifier[] getArgNames() {
		return type.getArgNames();
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		return ShortMessage.class;
	}

	@Override
	public void run(final Context context, final Position position) {
		final Identifier[] argNames = getArgNames();
		final MacObject[] args = new MacObject[argNames.length];
		for (int i = 0; i < argNames.length; i++) {
			args[i] = context.get(argNames[i]);
		}
		context.set(Identifiers.RESULT, type.create(args));
	}

	@Override
	public String toString() {
		return "Native function: create short message " + type;
	}

}
