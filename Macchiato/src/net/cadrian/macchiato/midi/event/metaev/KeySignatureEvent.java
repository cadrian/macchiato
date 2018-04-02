package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class KeySignatureEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitKeySignature(KeySignatureEvent event);
	}

	private final byte keysig;
	private final byte mode;

	public KeySignatureEvent(final byte keysig, final byte mode) {
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
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitKeySignature(this);
	}

}
