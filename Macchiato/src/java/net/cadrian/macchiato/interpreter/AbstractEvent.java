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

import java.math.BigInteger;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

import net.cadrian.macchiato.midi.Message;

public abstract class AbstractEvent {

	private final BigInteger tick;

	public AbstractEvent(final BigInteger tick) {
		this.tick = tick;
	}

	public BigInteger getTick() {
		return tick;
	}

	public abstract MidiMessage getMidiMessage();

	public abstract Message createMessage();

	public abstract MidiEvent createMidiEvent();

}
