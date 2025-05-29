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

public class ProgramChangeMessage extends ShortMessage {

	private final int patch;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Message.Visitor {
		void visitProgramChange(ProgramChangeMessage message);
	}

	public ProgramChangeMessage(final int channel, final int patch) {
		super(channel, ShortMessageType.PROGRAM_CHANGE);
		this.patch = patch;
	}

	public int getPatch() {
		return patch;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitProgramChange(this);
	}

	@Override
	public String toString() {
		return "PROGRAM_CHANGE(" + channel + ", " + patch + ")";
	}

}
