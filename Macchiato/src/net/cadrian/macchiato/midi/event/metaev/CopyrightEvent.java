package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class CopyrightEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitCopyright(CopyrightEvent event);
	}

	private final String text;

	public CopyrightEvent(final String text) {
		super(MetaMessageType.TEXT);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitCopyright(this);
	}

}
