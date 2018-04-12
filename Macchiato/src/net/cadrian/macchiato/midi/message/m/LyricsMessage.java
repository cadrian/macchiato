package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class LyricsMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitLyrics(LyricsMessage message);
	}

	public LyricsMessage(final String text) {
		super(MetaMessageType.LYRICS, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitLyrics(this);
	}

	@Override
	public String toString() {
		return "LYRICS(\"" + text + "\")";
	}

}
