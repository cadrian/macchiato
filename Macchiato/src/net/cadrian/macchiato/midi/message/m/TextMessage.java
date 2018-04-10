package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class TextMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitText(TextMessage message);
	}

	public TextMessage(final String text) {
		super(MetaMessageType.TEXT, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitText(this);
	}

}
