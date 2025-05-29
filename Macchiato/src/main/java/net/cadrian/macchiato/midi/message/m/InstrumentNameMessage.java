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

import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class InstrumentNameMessage extends AbstractTextMessage {

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Message.Visitor {
		void visitInstrumentName(InstrumentNameMessage message);
	}

	public InstrumentNameMessage(final MacString text) {
		super(MetaMessageType.INSTRUMENT_NAME, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitInstrumentName(this);
	}

	@Override
	public String toString() {
		return "INSTRUMENT_NAME(\"" + text + "\")";
	}

}
