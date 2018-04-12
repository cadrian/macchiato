package net.cadrian.macchiato.midi.message;

import java.math.BigInteger;

import javax.sound.midi.InvalidMidiDataException;

import net.cadrian.macchiato.interpreter.AbstractEvent;
import net.cadrian.macchiato.interpreter.MetaEvent;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public abstract class MetaMessage implements Message {

	private final MetaMessageType messageType;

	public MetaMessage(final MetaMessageType messageType) {
		this.messageType = messageType;
	}

	public MetaMessageType getMessageType() {
		return messageType;
	}

	@Override
	public AbstractEvent toEvent(final BigInteger tick) {
		try {
			return new MetaEvent(tick, messageType, messageType.createMidiMessage(this));
		} catch (final InvalidMidiDataException e) {
			throw new RuntimeException(e);
		}
	}

}
