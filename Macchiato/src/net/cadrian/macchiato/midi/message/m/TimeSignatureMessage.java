package net.cadrian.macchiato.midi.message.m;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.message.MetaMessage;

public class TimeSignatureMessage extends MetaMessage {

	public static interface Visitor extends Message.Visitor {
		void visitTimeSignature(TimeSignatureMessage message);
	}

	private final byte numerator;
	private final byte denominator;
	private final byte metronome;
	private final byte ticks;

	public TimeSignatureMessage(final byte n, final byte d, final byte m, final byte t) {
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
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitTimeSignature(this);
	}

	@Override
	public String toString() {
		return "TIME_SIGNATURE(" + numerator + ", " + denominator + ", " + metronome + ", " + ticks + ")";
	}

}
