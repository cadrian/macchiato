/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.midi;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.container.Dictionary;
import net.cadrian.macchiato.midi.message.s.ChannelPressureMessage;
import net.cadrian.macchiato.midi.message.s.ControlChangeMessage;
import net.cadrian.macchiato.midi.message.s.NoteOffMessage;
import net.cadrian.macchiato.midi.message.s.NoteOnMessage;
import net.cadrian.macchiato.midi.message.s.PitchBendMessage;
import net.cadrian.macchiato.midi.message.s.PolyPressureMessage;
import net.cadrian.macchiato.midi.message.s.ProgramChangeMessage;

public enum ShortMessageType {
	NOTE_OFF(0x80) {
		@Override
		public String toString(final int pitch, final int velocity) {
			return "pitch: " + pitch + " velocity: " + velocity;
		}

		@Override
		public Message<ShortMessage> createMessage(final int channel, final int pitch, final int velocity) {
			return new NoteOffMessage(channel, pitch, velocity);
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final NoteOffMessage m = (NoteOffMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPitch(), m.getVelocity());
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final NoteOffMessage m = (NoteOffMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("velocity", BigInteger.valueOf(m.getVelocity()));
			messageData.set("pitch", BigInteger.valueOf(m.getPitch()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT3;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_PITCH_VELOCITY;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final BigInteger velocity = (BigInteger) args[1];
			final BigInteger pitch = (BigInteger) args[2];
			return new NoteOffMessage(channel.intValueExact(), velocity.intValueExact(), pitch.intValueExact());
		}
	},
	NOTE_ON(0x90) {
		@Override
		public String toString(final int pitch, final int velocity) {
			return "pitch: " + pitch + " velocity: " + velocity;
		}

		@Override
		public Message<ShortMessage> createMessage(final int channel, final int pitch, final int velocity) {
			return new NoteOnMessage(channel, pitch, velocity);
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final NoteOnMessage m = (NoteOnMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPitch(), m.getVelocity());
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final NoteOnMessage m = (NoteOnMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("velocity", BigInteger.valueOf(m.getVelocity()));
			messageData.set("pitch", BigInteger.valueOf(m.getPitch()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT3;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_PITCH_VELOCITY;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final BigInteger velocity = (BigInteger) args[1];
			final BigInteger pitch = (BigInteger) args[2];
			return new NoteOnMessage(channel.intValueExact(), velocity.intValueExact(), pitch.intValueExact());
		}
	},
	POLY_PRESSURE(0xA0) {
		@Override
		public String toString(final int pressure, final int unused) {
			assert unused == 0;
			return "pressure: " + pressure;
		}

		@Override
		public Message<ShortMessage> createMessage(final int channel, final int pressure, final int unused) {
			return new PolyPressureMessage(channel, pressure);
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final PolyPressureMessage m = (PolyPressureMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPressure(), 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final PolyPressureMessage m = (PolyPressureMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("pressure", BigInteger.valueOf(m.getPressure()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT2;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_PRESSURE;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final BigInteger pressure = (BigInteger) args[1];
			return new PolyPressureMessage(channel.intValueExact(), pressure.intValueExact());
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
		public Message<ShortMessage> createMessage(final int channel, final int code, final int value) {
			final ControlChange mpc = ControlChange.at(code);
			if (mpc == null) {
				throw new NullPointerException("unknown MPC 0x" + Integer.toHexString(code));
			}
			return new ControlChangeMessage(channel, mpc, value);
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final ControlChangeMessage m = (ControlChangeMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getMpc().code, m.getValue());
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final ControlChangeMessage m = (ControlChangeMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			final ControlChange mpc = m.getMpc();
			messageData.set("mpc", mpc);
			messageData.set("value", mpc.valueOf(m.getValue()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_MPC;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_MPC_VALUE;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final ControlChange mpc = (ControlChange) args[1];
			return new ControlChangeMessage(channel.intValueExact(), mpc, mpc.midiValueOf(args[2]));
		}
	},
	PROGRAM_CHANGE(0xC0) {
		@Override
		public String toString(final int patch, final int unused) {
			assert unused == 0;
			return "patch: " + patch;
		}

		@Override
		public Message<ShortMessage> createMessage(final int channel, final int patch, final int unused) {
			return new ProgramChangeMessage(channel, patch);
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final ProgramChangeMessage m = (ProgramChangeMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPatch(), 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final ProgramChangeMessage m = (ProgramChangeMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("patch", BigInteger.valueOf(m.getPatch()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT2;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_PATCH;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final BigInteger patch = (BigInteger) args[1];
			return new ProgramChangeMessage(channel.intValueExact(), patch.intValueExact());
		}
	},
	CHANNEL_PRESSURE(0xD0) {
		@Override
		public String toString(final int pressure, final int unused) {
			assert unused == 0;
			return "pressure: " + pressure;
		}

		@Override
		public Message<ShortMessage> createMessage(final int channel, final int pressure, final int unused) {
			return new ChannelPressureMessage(channel, pressure);
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final ChannelPressureMessage m = (ChannelPressureMessage) message;
			return new ShortMessage(command, m.getChannel(), m.getPressure(), 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final ChannelPressureMessage m = (ChannelPressureMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("pressure", BigInteger.valueOf(m.getPressure()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT2;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_PRESSURE;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final BigInteger pressure = (BigInteger) args[1];
			return new ChannelPressureMessage(channel.intValueExact(), pressure.intValueExact());
		}
	},
	PITCH_BEND(0xE0) {
		@Override
		public String toString(final int data1, final int data2) {
			return Integer.toString(value(data1, data2));
		}

		@Override
		public Message<ShortMessage> createMessage(final int channel, final int data1, final int data2) {
			return new PitchBendMessage(channel, value(data1, data2));
		}

		private int value(final int data1, final int data2) {
			return (data1 << 7) + data2;
		}

		@Override
		public ShortMessage createMidiMessage(final Message<ShortMessage> message) throws InvalidMidiDataException {
			final PitchBendMessage m = (PitchBendMessage) message;
			final int data1 = (m.getValue() >>> 7);
			final int data2 = (m.getValue() & 0x7f);
			return new ShortMessage(command, m.getChannel(), data1, data2);
		}

		@Override
		public void fill(final Dictionary messageData, final Message<ShortMessage> message) {
			final PitchBendMessage m = (PitchBendMessage) message;
			messageData.set("channel", BigInteger.valueOf(m.getChannel()));
			messageData.set("value", BigInteger.valueOf(m.getValue()));
		}

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT2;
		}

		@Override
		public String[] getArgNames() {
			return ARG_CHANNEL_VALUE;
		}

		@Override
		public Message<ShortMessage> create(final Object... args) {
			final BigInteger channel = (BigInteger) args[0];
			final BigInteger value = (BigInteger) args[1];
			return new PitchBendMessage(channel.intValueExact(), value.intValueExact());
		}
	};

	private static final String[] ARG_CHANNEL_VALUE = new String[] { "channel", "value" };
	private static final String[] ARG_CHANNEL_PATCH = new String[] { "channel", "patch" };
	private static final String[] ARG_CHANNEL_MPC_VALUE = new String[] { "channel", "mpc", "value" };
	private static final String[] ARG_CHANNEL_PRESSURE = new String[] { "channel", "pressure" };
	private static final String[] ARG_CHANNEL_PITCH_VELOCITY = new String[] { "channel", "pitch", "velocity" };

	private static final Class<?>[] TYPE_MPC = new Class<?>[] { BigInteger.class, ControlChange.class,
			BigInteger.class };
	private static final Class<?>[] TYPE_INT2 = new Class<?>[] { BigInteger.class, BigInteger.class };
	private static final Class<?>[] TYPE_INT3 = new Class<?>[] { BigInteger.class, BigInteger.class, BigInteger.class };

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

	public abstract Message<ShortMessage> createMessage(int channel, int data1, int data2);

	public abstract ShortMessage createMidiMessage(Message<ShortMessage> message) throws InvalidMidiDataException;

	public abstract void fill(Dictionary messageData, Message<ShortMessage> message);

	public abstract Class<?>[] getArgTypes();

	public abstract String[] getArgNames();

	public abstract Message<ShortMessage> create(Object... args);

}
