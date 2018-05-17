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
package net.cadrian.macchiato.midi.message;

import java.math.BigInteger;

import javax.sound.midi.InvalidMidiDataException;

import net.cadrian.macchiato.interpreter.AbstractEvent;
import net.cadrian.macchiato.interpreter.MetaEvent;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public abstract class MetaMessage implements Message<javax.sound.midi.MetaMessage> {

	private final MetaMessageType messageType;

	public MetaMessage(final MetaMessageType messageType) {
		this.messageType = messageType;
	}

	public MetaMessageType getMessageType() {
		return messageType;
	}

	@Override
	public AbstractEvent<javax.sound.midi.MetaMessage> toEvent(final BigInteger tick) {
		try {
			return new MetaEvent(tick, messageType, messageType.createMidiMessage(this));
		} catch (final InvalidMidiDataException e) {
			throw new RuntimeException(e);
		}
	}

}
