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

public class ChannelPressureMessage extends ShortMessage {

	public interface Visitor extends Message.Visitor {
		void visitChannelPressure(ChannelPressureMessage message);
	}

	private final int pressure;

	public ChannelPressureMessage(final int channel, final int pressure) {
		super(channel, ShortMessageType.CHANNEL_PRESSURE);
		this.pressure = pressure;
	}

	public int getPressure() {
		return pressure;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitChannelPressure(this);
	}

	@Override
	public String toString() {
		return "CHANNEL_PRESSURE(" + channel + "," + pressure + ")";
	}

}
