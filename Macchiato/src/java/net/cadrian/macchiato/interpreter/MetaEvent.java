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

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class MetaEvent extends AbstractEvent {

	private final MetaMessageType type;
	private final MetaMessage midiMessage;

	public MetaEvent(final BigInteger tick, final MetaMessageType type, final MetaMessage message) {
		super(tick);
		if (type == null) {
			throw new NullPointerException("BUG: null type");
		}
		this.type = type;
		this.midiMessage = message;
	}

	public MetaMessageType getType() {
		return type;
	}

	@Override
	public MetaMessage getMidiMessage() {
		return midiMessage;
	}

	@Override
	public Message createMessage() {
		return type.createMessage(midiMessage.getData());
	}

	@Override
	public MidiEvent createMidiEvent() {
		return new MidiEvent(midiMessage, getTick().longValueExact());
	}

}
