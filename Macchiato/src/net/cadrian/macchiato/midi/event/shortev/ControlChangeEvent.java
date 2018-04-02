package net.cadrian.macchiato.midi.event.shortev;

import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Event;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.event.ShortEvent;

public class ControlChangeEvent extends ShortEvent {

	public static interface Visitor extends Event.Visitor {
		void visitControlChange(ControlChangeEvent event);
	}

	private final ControlChange mpc;
	private final int value;

	public ControlChangeEvent(final int channel, final ControlChange mpc, final int value) {
		super(channel, ShortMessageType.CONTROL_CHANGE);
		this.mpc = mpc;
		this.value = value;
	}

	public ControlChange getMpc() {
		return mpc;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(final Event.Visitor v) {
		((Visitor) v).visitControlChange(this);
	}

}
