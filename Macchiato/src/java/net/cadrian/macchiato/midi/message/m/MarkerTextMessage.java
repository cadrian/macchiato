package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class MarkerTextMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitMarkerText(MarkerTextMessage message);
	}

	public MarkerTextMessage(final String text) {
		super(MetaMessageType.MARKER_TEXT, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitMarkerText(this);
	}

	@Override
	public String toString() {
		return "MARKER_TEXT(\"" + text + "\")";
	}

}
