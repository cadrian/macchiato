package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class MarkerTextEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitMarkerText(MarkerTextEvent event);
	}

	private final String text;

	public MarkerTextEvent(final String text) {
		super(MetaMessageType.MARKER_TEXT);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitMarkerText(this);
	}

}
