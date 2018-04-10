package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

abstract class AbstractTextMessage extends MetaMessage {

	private final String text;

	public AbstractTextMessage(final MetaMessageType messageType, final String text) {
		super(messageType);
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
