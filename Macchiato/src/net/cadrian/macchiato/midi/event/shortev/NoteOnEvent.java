package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class NoteOnEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitNoteOn(NoteOnEvent event);
	}

	private final int pitch;
	private final int velocity;

	public NoteOnEvent(final int channel, final int pitch, final int velocity) {
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
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitNoteOn(this);
	}

}
