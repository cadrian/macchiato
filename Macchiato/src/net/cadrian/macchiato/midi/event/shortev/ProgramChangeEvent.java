package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class ProgramChangeEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitProgramChange(ProgramChangeEvent event);
	}

	private final int patch;

	public ProgramChangeEvent(final int channel, final int patch) {
		super(channel, ShortMessageType.PROGRAM_CHANGE);
		this.patch = patch;
	}

	public int getPatch() {
		return patch;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitProgramChange(this);
	}

}
