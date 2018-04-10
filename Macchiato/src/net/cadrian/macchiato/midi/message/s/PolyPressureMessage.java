package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class PolyPressureMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitPolyPressure(PolyPressureMessage message);
	}

	private final int pressure;

	public PolyPressureMessage(final int channel, final int pressure) {
		super(channel, ShortMessageType.POLY_PRESSURE);
		this.pressure = pressure;
	}

	public int getPressure() {
		return pressure;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitPolyPressure(this);
	}

}
