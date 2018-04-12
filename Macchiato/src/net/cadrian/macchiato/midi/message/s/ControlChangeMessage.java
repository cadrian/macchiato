package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class ControlChangeMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitControlChange(ControlChangeMessage message);
	}

	private final ControlChange mpc;
	private final int value;

	public ControlChangeMessage(final int channel, final ControlChange mpc, final int value) {
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
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitControlChange(this);
	}

	@Override
	public String toString() {
		return "CONTROL_CHANGE(" + channel + ", " + mpc + ", " + value + ")";
	}

}
