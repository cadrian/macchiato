package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class TextEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitText(TextEvent event);
	}

	private final String text;

	public TextEvent(final String text) {
		super(MetaMessageType.COPYRIGHT);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitText(this);
	}

}
