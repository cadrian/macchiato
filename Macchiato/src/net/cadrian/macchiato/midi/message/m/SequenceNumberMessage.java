package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

public class SequenceNumberMessage extends MetaMessage {

	public static interface Visitor extends Message.Visitor {
		void visitSequenceNumberEvent(SequenceNumberMessage message);
	}

	private final int sequence;

	public SequenceNumberMessage(final int sequence) {
		super(MetaMessageType.SEQUENCE_NUMBER);
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitSequenceNumberEvent(this);
	}

	@Override
	public String toString() {
		return "SEQUENCE_NUMBER(" + sequence + ")";
	}

}
