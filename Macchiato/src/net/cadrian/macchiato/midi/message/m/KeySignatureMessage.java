package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

public class KeySignatureMessage extends MetaMessage {

	public static interface Visitor extends Message.Visitor {
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

}
