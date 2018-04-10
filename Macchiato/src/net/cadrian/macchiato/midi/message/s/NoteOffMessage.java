package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class NoteOffMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitNoteOff(NoteOffMessage message);
	}

	private final int pitch;
	private final int velocity;

	public NoteOffMessage(final int channel, final int pitch, final int velocity) {
		super(channel, ShortMessageType.NOTE_OFF);
		this.pitch = pitch;
		this.velocity = velocity;
	}

	public int getPitch() {
		return pitch;
	}

	public int getVelocity() {
		return velocity;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitNoteOff(this);
	}

}
