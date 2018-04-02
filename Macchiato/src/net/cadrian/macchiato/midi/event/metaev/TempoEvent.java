package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class TempoEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitTempo(TempoEvent event);
	}

	private final int bpm;

	public TempoEvent(final int bpm) {
		super(MetaMessageType.TEMPO);
		this.bpm = bpm;
	}

	public int getBpm() {
		return bpm;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitTempo(this);
	}

}
