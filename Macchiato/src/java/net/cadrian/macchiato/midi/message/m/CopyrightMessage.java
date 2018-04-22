package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class CopyrightMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitCopyright(CopyrightMessage message);
	}

	public CopyrightMessage(final String text) {
		super(MetaMessageType.COPYRIGHT, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitCopyright(this);
	}

	@Override
	public String toString() {
		return "COPYRIGHT(\"" + text + "\")";
	}

}
