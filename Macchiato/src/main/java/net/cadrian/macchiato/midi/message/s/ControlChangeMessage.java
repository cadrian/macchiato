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

import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class ControlChangeMessage extends ShortMessage {

	private final ControlChange mpc;
	private final int value;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Message.Visitor {
		void visitControlChange(ControlChangeMessage message);
	}

	public ControlChangeMessage(final int channel, final ControlChange mpc, final int value) {
		super(channel, ShortMessageType.CONTROL_CHANGE);
		if (mpc == null) {
			throw new IllegalArgumentException("unknown MPC");
		}
		this.mpc = mpc;
		this.value = value;
	}

	public ControlChange getMpc() {
		return mpc;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitControlChange(this);
	}

	@Override
	public String toString() {
		return "CONTROL_CHANGE(" + channel + ", " + mpc + ", " + value + ")";
	}

}
