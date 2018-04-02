package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class InstrumentNameEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitInstrumentName(InstrumentNameEvent event);
	}

	private final String text;

	public InstrumentNameEvent(final String text) {
		super(MetaMessageType.INSTRUMENT_NAME);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitInstrumentName(this);
	}

}
