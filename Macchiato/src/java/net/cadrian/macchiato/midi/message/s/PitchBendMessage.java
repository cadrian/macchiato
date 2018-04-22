package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class PitchBendMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitPitchBend(PitchBendMessage message);
	}

	private final int value;

	public PitchBendMessage(final int channel, final int value) {
		super(channel, ShortMessageType.PITCH_BEND);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitPitchBend(this);
	}

	@Override
	public String toString() {
		return "PITCH_BEND(" + channel + ", " + value + ")";
	}

}
