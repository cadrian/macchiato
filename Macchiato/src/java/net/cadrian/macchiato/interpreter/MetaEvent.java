package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class MetaEvent extends AbstractEvent {

	private final MetaMessageType type;
	private final MetaMessage midiMessage;

	public MetaEvent(final BigInteger tick, final MetaMessageType type, final MetaMessage message) {
		super(tick);
		this.type = type;
		this.midiMessage = message;
	}

	public MetaMessageType getType() {
		return type;
	}

	@Override
	public MetaMessage getMidiMessage() {
		return midiMessage;
	}

	@Override
	public Message createMessage() {
		return type.createMessage(midiMessage.getData());
	}

	@Override
	public MidiEvent createMidiEvent() {
		return new MidiEvent(midiMessage, getTick().longValueExact());
	}

}
