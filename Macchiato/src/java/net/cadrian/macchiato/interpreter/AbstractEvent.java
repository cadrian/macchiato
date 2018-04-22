package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

import net.cadrian.macchiato.midi.Message;

public abstract class AbstractEvent {

	private final BigInteger tick;

	public AbstractEvent(final BigInteger tick) {
		this.tick = tick;
	}

	public BigInteger getTick() {
		return tick;
	}

	public abstract MidiMessage getMidiMessage();

	public abstract Message createMessage();

	public abstract MidiEvent createMidiEvent();

}
