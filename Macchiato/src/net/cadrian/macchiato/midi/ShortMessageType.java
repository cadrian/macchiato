package net.cadrian.macchiato.midi;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.message.s.ChannelPressureMessage;
import net.cadrian.macchiato.midi.message.s.ControlChangeMessage;
import net.cadrian.macchiato.midi.message.s.NoteOffMessage;
import net.cadrian.macchiato.midi.message.s.NoteOnMessage;
import net.cadrian.macchiato.midi.message.s.PitchBendMessage;
import net.cadrian.macchiato.midi.message.s.PolyPressureMessage;
import net.cadrian.macchiato.midi.message.s.ProgramChangeMessage;
import net.cadrian.macchiato.recipe.interpreter.Dictionary;

public enum ShortMessageType {
	NOTE_OFF(0x80) {
		@Override
		public String toString(final int pitch, final int velocity) {
			return "pitch: " + pitch + " velocity: " + velocity;
		}

		@Override
		public Message createMessage(final int channel, final int pitch, final int velocity) {
			return new NoteOffMessage(channel, pitch, velocity);
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final NoteOffMessage m = (NoteOffMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPitch(), m.getVelocity());
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final NoteOffMessage m = (NoteOffMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("velocity", BigInteger.valueOf(m.getVelocity()));
			messageData.set("pitch", BigInteger.valueOf(m.getPitch()));
		}
	},
	NOTE_ON(0x90) {
		@Override
		public String toString(final int pitch, final int velocity) {
			return "pitch: " + pitch + " velocity: " + velocity;
		}

		@Override
		public Message createMessage(final int channel, final int pitch, final int velocity) {
			return new NoteOnMessage(channel, pitch, velocity);
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final NoteOnMessage m = (NoteOnMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPitch(), m.getVelocity());
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final NoteOnMessage m = (NoteOnMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("velocity", BigInteger.valueOf(m.getVelocity()));
			messageData.set("pitch", BigInteger.valueOf(m.getPitch()));
		}
	},
	POLY_PRESSURE(0xA0) {
		@Override
		public String toString(final int pressure, final int unused) {
			assert unused == 0;
			return "pressure: " + pressure;
		}

		@Override
		public Message createMessage(final int channel, final int pressure, final int unused) {
			return new PolyPressureMessage(channel, pressure);
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final PolyPressureMessage m = (PolyPressureMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPressure(), 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final PolyPressureMessage m = (PolyPressureMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("pressure", BigInteger.valueOf(m.getPressure()));
		}
	},
	CONTROL_CHANGE(0xB0) {
		@Override
		public String toString(final int code, final int value) {
			final ControlChange mpc = ControlChange.at(code);
			if (mpc != null) {
				return mpc + ": " + mpc.toString(value);
			}
			return code + ": " + value;
		}

		@Override
		public Message createMessage(final int channel, final int code, final int value) {
			return new ControlChangeMessage(channel, ControlChange.at(code), value);
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final ControlChangeMessage m = (ControlChangeMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getMpc().code, m.getValue());
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final ControlChangeMessage m = (ControlChangeMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("mpc", m.getMpc());
			messageData.set("value", BigInteger.valueOf(m.getValue()));
		}
	},
	PROGRAM_CHANGE(0xC0) {
		@Override
		public String toString(final int patch, final int unused) {
			assert unused == 0;
			return "patch: " + patch;
		}

		@Override
		public Message createMessage(final int channel, final int patch, final int unused) {
			return new ProgramChangeMessage(channel, patch);
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final ProgramChangeMessage m = (ProgramChangeMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPatch(), 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final ProgramChangeMessage m = (ProgramChangeMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("patch", BigInteger.valueOf(m.getPatch()));
		}
	},
	CHANNEL_PRESSURE(0xD0) {
		@Override
		public String toString(final int pressure, final int unused) {
			assert unused == 0;
			return "pressure: " + pressure;
		}

		@Override
		public Message createMessage(final int channel, final int pressure, final int unused) {
			return new ChannelPressureMessage(channel, pressure);
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final ChannelPressureMessage m = (ChannelPressureMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPressure(), 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final ChannelPressureMessage m = (ChannelPressureMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("pressure", BigInteger.valueOf(m.getPressure()));
		}
	},
	PITCH_BEND(0xE0) {
		@Override
		public String toString(final int data1, final int data2) {
			return Integer.toString(value(data1, data2));
		}

		@Override
		public Message createMessage(final int channel, final int data1, final int data2) {
			return new PitchBendMessage(channel, value(data1, data2));
		}

		private int value(final int data1, final int data2) {
			return (data1 << 7) + data2;
		}

		@Override
		public ShortMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final PitchBendMessage m = (PitchBendMessage) message;
			final int data1 = (m.getValue() >>> 7);
			final int data2 = (m.getValue() & 0x7f);
			return new ShortMessage(command, m.getChannel(), data1, data2);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final PitchBendMessage m = (PitchBendMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("value", BigInteger.valueOf(m.getValue()));
		}
	};

	private static final Map<Integer, ShortMessageType> MAP;
	static {
		final Map<Integer, ShortMessageType> m = new HashMap<>();
		for (final ShortMessageType type : values()) {
			m.put(type.command, type);
		}
		MAP = Collections.unmodifiableMap(m);
	}

	public final int command;

	private ShortMessageType(final int command) {
		this.command = command;
	}

	public static ShortMessageType at(final int command) {
		return MAP.get(command);
	}

	public abstract String toString(int data1, int data2);

	public abstract Message createMessage(int channel, int data1, int data2);

	public abstract ShortMessage createMidiMessage(Message message) throws InvalidMidiDataException;

	public abstract void fill(Dictionary messageData, Message message);

}
