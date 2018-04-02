package net.cadrian.macchiato.midi.event;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;

public abstract class ShortEvent implements Event {

	private final ShortMessageType messageType;
	private final int channel;

	public ShortEvent(final int channel, final ShortMessageType messageType) {
		this.messageType = messageType;
		this.channel = channel;
	}

	public ShortMessageType getMessageType() {
		return messageType;
	}

	public int getChannel() {
		return channel;
	}

}
