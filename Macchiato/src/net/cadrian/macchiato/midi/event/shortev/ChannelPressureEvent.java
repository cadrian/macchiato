package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class ChannelPressureEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitChannelPressure(ChannelPressureEvent event);
	}

	private final int pressure;

	public ChannelPressureEvent(final int channel, final int pressure) {
		super(channel, ShortMessageType.PROGRAM_CHANGE);
		this.pressure = pressure;
	}

	public int getPressure() {
		return pressure;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitChannelPressure(this);
		;
	}

}
