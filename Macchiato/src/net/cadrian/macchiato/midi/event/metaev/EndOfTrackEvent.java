package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class EndOfTrackEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitEndOfTrack(EndOfTrackEvent event);
	}

	public EndOfTrackEvent() {
		super(MetaMessageType.END_OF_TRACK);
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitEndOfTrack(this);
	}

}
