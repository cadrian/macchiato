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
package net.cadrian.macchiato.interpreter.event;

import javax.sound.midi.MidiMessage;

import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.objects.MacNumber;

abstract class AbstractEvent<M extends MidiMessage> implements Event<M> {

	protected final MacNumber tick;
	protected final M midiMessage;

	AbstractEvent(final MacNumber tick, final M midiMessage) {
		if (tick == null) {
			throw new NullPointerException("BUG: null tick");
		}
		if (midiMessage == null) {
			throw new NullPointerException("BUG: null midiMessage");
		}
		this.tick = tick;
		this.midiMessage = midiMessage;
	}

	@Override
	public MacNumber getTick() {
		return tick;
	}

	@Override
	public M getMidiMessage() {
		return midiMessage;
	}

	@Override
	public String toString() {
		return midiMessage + " at " + tick;
	}

}
