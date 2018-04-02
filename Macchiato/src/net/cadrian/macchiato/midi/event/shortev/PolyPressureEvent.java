package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class PolyPressureEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitPolyPressure(PolyPressureEvent event);
	}

	private final int pressure;

	public PolyPressureEvent(final int channel, final int pressure) {
		super(channel, ShortMessageType.POLY_PRESSURE);
		this.pressure = pressure;
	}

	public int getPressure() {
		return pressure;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitPolyPressure(this);
	}

}
