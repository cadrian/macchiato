package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class LyricsEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitLyrics(LyricsEvent event);
	}

	private final String text;

	public LyricsEvent(final String text) {
		super(MetaMessageType.LYRICS);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitLyrics(this);
	}

}
