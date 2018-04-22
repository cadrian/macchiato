package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class ProgramChangeMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitProgramChange(ProgramChangeMessage message);
	}

	private final int patch;

	public ProgramChangeMessage(final int channel, final int patch) {
		super(channel, ShortMessageType.PROGRAM_CHANGE);
		this.patch = patch;
	}

	public int getPatch() {
		return patch;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitProgramChange(this);
	}

	@Override
	public String toString() {
		return "PROGRAM_CHANGE(" + channel + ", " + patch + ")";
	}

}
