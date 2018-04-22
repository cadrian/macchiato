package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;

public class ShortEvent extends AbstractEvent {

	private final ShortMessageType type;
	private final ShortMessage midiMessage;

	public ShortEvent(final BigInteger tick, final ShortMessageType type, final ShortMessage message) {
		super(tick);
		this.type = type;
		this.midiMessage = message;
	}

	public ShortMessageType getType() {
		return type;
	}

	@Override
	public ShortMessage getMidiMessage() {
		return midiMessage;
	}

	@Override
	public Message createMessage() {
		return type.createMessage(midiMessage.getChannel(), midiMessage.getData1(), midiMessage.getData2());
	}

	@Override
	public MidiEvent createMidiEvent() {
		return new MidiEvent(midiMessage, getTick().longValueExact());
	}

}
