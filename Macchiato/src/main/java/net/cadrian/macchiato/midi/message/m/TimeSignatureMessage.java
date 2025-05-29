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

public class TimeSignatureMessage extends MetaMessage {

	private final byte numerator;
	private final byte denominator;
	private final byte metronome;
	private final byte ticks;

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Message.Visitor {
		void visitTimeSignature(TimeSignatureMessage message);
	}

	public TimeSignatureMessage(final byte n, final byte d, final byte m, final byte t) {
		super(MetaMessageType.TIME_SIGNATURE);
		numerator = n;
		denominator = d;
		metronome = m;
		ticks = t;
	}

	public byte getNumerator() {
		return numerator;
	}

	public byte getDenominator() {
		return denominator;
	}

	public byte getMetronome() {
		return metronome;
	}

	public byte getTicks() {
		return ticks;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitTimeSignature(this);
	}

	@Override
	public String toString() {
		return "TIME_SIGNATURE(" + numerator + ", " + denominator + ", " + metronome + ", " + ticks + ")";
	}

}
