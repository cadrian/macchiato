package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class CuePointEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitCuePoint(CuePointEvent event);
	}

	private final String text;

	public CuePointEvent(final String text) {
		super(MetaMessageType.CUE_POINT);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitCuePoint(this);
	}

}
