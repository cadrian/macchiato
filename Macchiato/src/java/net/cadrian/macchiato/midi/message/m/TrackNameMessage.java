package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class TrackNameMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitTrackName(TrackNameMessage message);
	}

	public TrackNameMessage(final String text) {
		super(MetaMessageType.TRACK_NAME, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitTrackName(this);
	}

	@Override
	public String toString() {
		return "TRACK_NAME(\"" + text + "\")";
	}

}
