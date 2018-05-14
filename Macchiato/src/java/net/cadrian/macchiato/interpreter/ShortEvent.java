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
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;

public class ShortEvent extends AbstractEvent {

	private final ShortMessageType type;
	private final ShortMessage midiMessage;

	public ShortEvent(final BigInteger tick, final ShortMessageType type, final ShortMessage message) {
		super(tick);
		if (type == null) {
			throw new NullPointerException("BUG: null type");
		}
		this.type = type;
		this.midiMessage = message;
	}

	public ShortMessageType getType() {
		return type;
	}

	@Override
	public ShortMessage getMidiMessage() {
		return midiMessage;
	}

	@Override
	public Message createMessage() {
		return type.createMessage(midiMessage.getChannel(), midiMessage.getData1(), midiMessage.getData2());
	}

	@Override
	public MidiEvent createMidiEvent() {
		return new MidiEvent(midiMessage, getTick().longValueExact());
	}

}
