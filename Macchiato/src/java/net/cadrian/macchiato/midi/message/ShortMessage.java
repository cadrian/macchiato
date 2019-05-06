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

import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.event.ShortEvent;
import net.cadrian.macchiato.interpreter.objects.MacComparable;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public abstract class ShortMessage implements MacComparable<ShortMessage>, Message<javax.sound.midi.ShortMessage> {

	private final ShortMessageType messageType;
	protected final int channel;

	public ShortMessage(final int channel, final ShortMessageType messageType) {
		this.messageType = messageType;
		this.channel = channel;
	}

	public ShortMessageType getMessageType() {
		return messageType;
	}

	public int getChannel() {
		return channel;
	}

	@Override
	public Event<javax.sound.midi.ShortMessage> toEvent(final BigInteger tick) {
		try {
			return new ShortEvent(MacNumber.valueOf(tick), messageType, messageType.createMidiMessage(this));
		} catch (final InvalidMidiDataException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final String name) {
		return null;
	}

	@Override
	public int compareTo(final ShortMessage other) {
		return messageType.compareTo(other.messageType);
	}

}
