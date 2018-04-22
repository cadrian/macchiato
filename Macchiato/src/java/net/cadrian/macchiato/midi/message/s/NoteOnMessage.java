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
package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class NoteOnMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitNoteOn(NoteOnMessage message);
	}

	private final int pitch;
	private final int velocity;

	public NoteOnMessage(final int channel, final int pitch, final int velocity) {
		super(channel, ShortMessageType.NOTE_ON);
		this.pitch = pitch;
		this.velocity = velocity;
	}

	public int getPitch() {
		return pitch;
	}

	public int getVelocity() {
		return velocity;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitNoteOn(this);
	}

	@Override
	public String toString() {
		return "NOTE_ON(" + channel + ", " + pitch + ", " + velocity + ")";
	}

}
