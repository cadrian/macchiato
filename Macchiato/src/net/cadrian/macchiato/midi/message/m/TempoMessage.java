package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

public class TempoMessage extends MetaMessage {

	public static interface Visitor extends Message.Visitor {
		void visitTempo(TempoMessage message);
	}

	private final int bpm;

	public TempoMessage(final int bpm) {
		super(MetaMessageType.TEMPO);
		this.bpm = bpm;
	}

	public int getBpm() {
		return bpm;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitTempo(this);
	}

}
