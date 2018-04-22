package net.cadrian.macchiato.midi.message.s;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.midi.message.ShortMessage;

public class ChannelPressureMessage extends ShortMessage {

	public static interface Visitor extends Message.Visitor {
		void visitChannelPressure(ChannelPressureMessage message);
	}

	private final int pressure;

	public ChannelPressureMessage(final int channel, final int pressure) {
		super(channel, ShortMessageType.CHANNEL_PRESSURE);
		this.pressure = pressure;
	}

	public int getPressure() {
		return pressure;
	}

	@Override
	public void accept(final Message.Visitor v) {
		((Visitor) v).visitChannelPressure(this);
	}

	@Override
	public String toString() {
		return "CHANNEL_PRESSURE(" + channel + "," + pressure + ")";
	}

}
