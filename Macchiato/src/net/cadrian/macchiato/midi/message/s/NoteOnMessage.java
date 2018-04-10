package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class NoteOnMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitNoteOn(NoteOnMessage message);
	}

	private final int pitch;
	private final int velocity;

	public NoteOnMessage(final int channel, final int pitch, final int velocity) {
		super(channel, ShortMessageType.NOTE_ON);
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
		((Visitor) v).visitNoteOn(this);
	}

}
