package net.cadrian.macchiato.midi.event;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;

public abstract class MetaEvent implements Event {

	private final MetaMessageType messageType;

	public MetaEvent(final MetaMessageType messageType) {
		this.messageType = messageType;
	}

	public MetaMessageType getMessageType() {
		return messageType;
	}

}
