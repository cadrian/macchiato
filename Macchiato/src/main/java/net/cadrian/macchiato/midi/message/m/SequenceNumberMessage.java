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
package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

public class SequenceNumberMessage extends MetaMessage {

	public interface Visitor extends Message.Visitor {
		void visitSequenceNumber(SequenceNumberMessage message);
	}

	private final int sequence;

	public SequenceNumberMessage(final int sequence) {
		super(MetaMessageType.SEQUENCE_NUMBER);
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitSequenceNumber(this);
	}

	@Override
	public String toString() {
		return "SEQUENCE_NUMBER(" + sequence + ")";
	}

}
