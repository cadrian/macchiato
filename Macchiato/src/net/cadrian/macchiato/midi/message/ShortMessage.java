package net.cadrian.macchiato.midi.message;

import java.math.BigInteger;

import javax.sound.midi.InvalidMidiDataException;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.recipe.interpreter.AbstractEvent;
import net.cadrian.macchiato.recipe.interpreter.ShortEvent;

public abstract class ShortMessage implements Message {

	private final ShortMessageType messageType;
	private final int channel;

	public ShortMessage(final int channel, final ShortMessageType messageType) {
		this.messageType = messageType;
		this.channel = channel;
	}

	public ShortMessageType getMessageType() {
		return messageType;
	}

	public int getChannel() {
		return channel;
	}

	@Override
	public AbstractEvent toEvent(final BigInteger tick) {
		try {
			return new ShortEvent(tick, messageType, messageType.createMidiMessage(this));
		} catch (final InvalidMidiDataException e) {
			throw new RuntimeException(e);
		}
	}

}
