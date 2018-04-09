package net.cadrian.macchiato.recipe.interpreter;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;

class ShortEvent extends AbstractEvent {

	private final ShortMessageType type;
	private final ShortMessage message;

	public ShortEvent(final int index, final long tick, final ShortMessageType type, final ShortMessage message) {
		super(index, tick);
		this.type = type;
		this.message = message;
	}

	public ShortMessageType getType() {
		return type;
	}

	public ShortMessage getMessage() {
		return message;
	}

	@Override
	public Event asEvent() {
		return type.createEvent(message.getChannel(), message.getData1(), message.getData2());
	}

	@Override
	public MidiEvent asMidi() {
		return new MidiEvent(message, getTick());
	}

}
