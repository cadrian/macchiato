package net.cadrian.macchiato.midi.message;

import java.math.BigInteger;

import javax.sound.midi.InvalidMidiDataException;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.recipe.interpreter.AbstractEvent;
import net.cadrian.macchiato.recipe.interpreter.MetaEvent;

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
