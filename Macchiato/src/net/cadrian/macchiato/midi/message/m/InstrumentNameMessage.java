package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;

public class InstrumentNameMessage extends AbstractTextMessage {

	public static interface Visitor extends Message.Visitor {
		void visitInstrumentName(InstrumentNameMessage message);
	}

	public InstrumentNameMessage(final String text) {
		super(MetaMessageType.INSTRUMENT_NAME, text);
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitInstrumentName(this);
	}

	@Override
	public String toString() {
		return "INSTRUMENT_NAME(\"" + text + "\")";
	}

}
