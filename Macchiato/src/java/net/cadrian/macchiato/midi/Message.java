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
package net.cadrian.macchiato.midi;

import java.math.BigInteger;

import javax.sound.midi.MidiMessage;

import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public interface Message<M extends MidiMessage> extends MacObject {

	interface Visitor {
	}

	void accept(Visitor v);

	Event<M> toEvent(BigInteger tick);

	@Override
	default <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		return null;
	}

}
