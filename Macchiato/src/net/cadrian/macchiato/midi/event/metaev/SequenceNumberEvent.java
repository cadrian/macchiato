package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class SequenceNumberEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitSequenceNumberEvent(SequenceNumberEvent event);
	}

	private final int sequence;

	public SequenceNumberEvent(final int sequence) {
		super(MetaMessageType.SEQUENCE_NUMBER);
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitSequenceNumberEvent(this);
	}

}
