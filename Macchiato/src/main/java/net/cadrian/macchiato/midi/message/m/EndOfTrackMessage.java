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

public class EndOfTrackMessage extends MetaMessage {

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Message.Visitor {
		void visitEndOfTrack(EndOfTrackMessage message);
	}

	public EndOfTrackMessage() {
		super(MetaMessageType.END_OF_TRACK);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitEndOfTrack(this);
	}

	@Override
	public String toString() {
		return "END_OF_TRACK()";
	}

}
