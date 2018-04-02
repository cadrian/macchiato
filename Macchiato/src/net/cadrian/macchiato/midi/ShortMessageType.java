package net.cadrian.macchiato.midi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.cadrian.macchiato.midi.event.shortev.ChannelPressureEvent;
import net.cadrian.macchiato.midi.event.shortev.ControlChangeEvent;
import net.cadrian.macchiato.midi.event.shortev.NoteOffEvent;
import net.cadrian.macchiato.midi.event.shortev.NoteOnEvent;
import net.cadrian.macchiato.midi.event.shortev.PitchBendEvent;
import net.cadrian.macchiato.midi.event.shortev.PolyPressureEvent;
import net.cadrian.macchiato.midi.event.shortev.ProgramChangeEvent;

public enum ShortMessageType {
	NOTE_OFF(0x80) {
		@Override
		public String toString(final int pitch, final int velocity) {
			return "pitch: " + pitch + " velocity: " + velocity;
		}

		@Override
		public Event createEvent(final int channel, final int pitch, final int velocity) {
			return new NoteOffEvent(channel, pitch, velocity);
		}
	},
	NOTE_ON(0x90) {
		@Override
		public String toString(final int pitch, final int velocity) {
			return "pitch: " + pitch + " velocity: " + velocity;
		}

		@Override
		public Event createEvent(final int channel, final int pitch, final int velocity) {
			return new NoteOnEvent(channel, pitch, velocity);
		}
	},
	POLY_PRESSURE(0xA0) {
		@Override
		public String toString(final int pressure, final int unused) {
			assert unused == 0;
			return "pressure: " + pressure;
		}

		@Override
		public Event createEvent(final int channel, final int pressure, final int unused) {
			return new PolyPressureEvent(channel, pressure);
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
		public Event createEvent(final int channel, final int code, final int value) {
			return new ControlChangeEvent(channel, ControlChange.at(code), value);
		}
	},
	PROGRAM_CHANGE(0xC0) {
		@Override
		public String toString(final int patch, final int unused) {
			assert unused == 0;
			return "patch: " + patch;
		}

		@Override
		public Event createEvent(final int channel, final int patch, final int unused) {
			return new ProgramChangeEvent(channel, patch);
		}
	},
	CHANNEL_PRESSURE(0xD0) {
		@Override
		public String toString(final int pressure, final int unused) {
			assert unused == 0;
			return "pressure: " + pressure;
		}

		@Override
		public Event createEvent(final int channel, final int pressure, final int unused) {
			return new ChannelPressureEvent(channel, pressure);
		}
	},
	PITCH_BEND(0xE0) {
		@Override
		public String toString(final int data1, final int data2) {
			return Integer.toString(value(data1, data2));
		}

		@Override
		public Event createEvent(final int channel, final int data1, final int data2) {
			return new PitchBendEvent(channel, value(data1, data2));
		}

		private int value(final int data1, final int data2) {
			return (data1 << 7) + data2;
		}
	};

	private static final Map<Byte, ShortMessageType> MAP;
	static {
		final Map<Byte, ShortMessageType> m = new HashMap<>();
		for (final ShortMessageType type : values()) {
			m.put(type.status, type);
		}
		MAP = Collections.unmodifiableMap(m);
	}

	public final byte status;

	private ShortMessageType(final int status) {
		this.status = (byte) status;
	}

	public static ShortMessageType at(final int status) {
		return MAP.get((byte) status);
	}

	public abstract String toString(int data1, int data2);

	public abstract Event createEvent(int channel, int data1, int data2);

}
