package net.cadrian.macchiato.midi.event.metaev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.event.MetaEvent;

public class TimeSignatureEvent extends MetaEvent {

	public static interface Visitor extends Event.Visitor {
		void visitTimeSignature(TimeSignatureEvent event);
	}

	private final byte numerator;
	private final byte denominator;
	private final byte metronome;
	private final byte ticks;

	public TimeSignatureEvent(final byte n, final byte d, final byte m, final byte t) {
		super(MetaMessageType.TIME_SIGNATURE);
		this.numerator = n;
		this.denominator = d;
		this.metronome = m;
		this.ticks = t;
	}

	public byte getNumerator() {
		return numerator;
	}

	public byte getDenominator() {
		return denominator;
	}

	public byte getMetronome() {
		return metronome;
	}

	public byte getTicks() {
		return ticks;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitTimeSignature(this);
	}

}
