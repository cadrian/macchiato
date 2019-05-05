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

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class MetaEvent extends AbstractEvent<MetaMessage> {

	private final MetaMessageType type;

	public MetaEvent(final MacNumber tick, final MetaMessageType type, final MetaMessage message) {
		super(tick, message);
		if (type == null) {
			throw new NullPointerException("BUG: null type");
		}
		this.type = type;
	}

	public MetaMessageType getType() {
		return type;
	}

	@Override
	public Message<MetaMessage> createMessage() {
		return type.createMessage(midiMessage.getData());
	}

	@Override
	public MidiEvent createMidiEvent() {
		return new MidiEvent(midiMessage, tick.getValue().longValueExact());
	}

	@Override
	public String toString() {
		return midiMessage + " at " + tick;
	}

}
