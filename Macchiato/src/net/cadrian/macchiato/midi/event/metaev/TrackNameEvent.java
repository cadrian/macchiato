package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class TrackNameEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitTrackName(TrackNameEvent event);
	}

	private final String text;

	public TrackNameEvent(final String text) {
		super(MetaMessageType.TRACK_NAME);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitTrackName(this);
	}

}
