package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class NoteOffEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitNoteOff(NoteOffEvent event);
	}

	private final int pitch;
	private final int velocity;

	public NoteOffEvent(final int channel, final int pitch, final int velocity) {
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
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitNoteOff(this);
	}

}
