package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class CuePointMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitCuePoint(CuePointMessage message);
	}

	public CuePointMessage(final String text) {
		super(MetaMessageType.CUE_POINT, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitCuePoint(this);
	}

}
