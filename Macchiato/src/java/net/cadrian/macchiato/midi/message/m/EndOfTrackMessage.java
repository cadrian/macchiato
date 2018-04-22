package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

public class EndOfTrackMessage extends MetaMessage {

	public static interface Visitor extends Message.Visitor {
		void visitEndOfTrack(EndOfTrackMessage message);
	}

	public EndOfTrackMessage() {
		super(MetaMessageType.END_OF_TRACK);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitEndOfTrack(this);
	}

	@Override
	public String toString() {
		return "END_OF_TRACK()";
	}

}
