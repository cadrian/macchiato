package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class PitchBendEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitPitchBend(PitchBendEvent event);
	}

	private final int value;

	public PitchBendEvent(final int channel, final int value) {
		super(channel, ShortMessageType.PROGRAM_CHANGE);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitPitchBend(this);
	}

}
