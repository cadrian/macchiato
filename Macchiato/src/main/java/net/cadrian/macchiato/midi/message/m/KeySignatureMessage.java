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

public class KeySignatureMessage extends MetaMessage {

	public interface Visitor extends Message.Visitor {
		void visitKeySignature(KeySignatureMessage message);
	}

	private final byte keysig;
	private final byte mode;

	public KeySignatureMessage(final byte keysig, final byte mode) {
		super(MetaMessageType.KEY_SIGNATURE);
		this.keysig = keysig;
		this.mode = mode;
	}

	public byte getKeysig() {
		return keysig;
	}

	public byte getMode() {
		return mode;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitKeySignature(this);
	}

	@Override
	public String toString() {
		return "KEY_SIGNATURE(" + keysig + ", " + mode + ")";
	}

}
