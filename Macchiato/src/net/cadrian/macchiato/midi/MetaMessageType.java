package net.cadrian.macchiato.midi;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

import net.cadrian.macchiato.midi.message.m.CopyrightMessage;
import net.cadrian.macchiato.midi.message.m.CuePointMessage;
import net.cadrian.macchiato.midi.message.m.EndOfTrackMessage;
import net.cadrian.macchiato.midi.message.m.InstrumentNameMessage;
import net.cadrian.macchiato.midi.message.m.KeySignatureMessage;
import net.cadrian.macchiato.midi.message.m.LyricsMessage;
import net.cadrian.macchiato.midi.message.m.MarkerTextMessage;
import net.cadrian.macchiato.midi.message.m.SequenceNumberMessage;
import net.cadrian.macchiato.midi.message.m.TempoMessage;
import net.cadrian.macchiato.midi.message.m.TextMessage;
import net.cadrian.macchiato.midi.message.m.TimeSignatureMessage;
import net.cadrian.macchiato.midi.message.m.TrackNameMessage;
import net.cadrian.macchiato.recipe.interpreter.Dictionary;

public enum MetaMessageType {
	SEQUENCE_NUMBER(0x00) {
		@Override
		public String toString(final byte[] data) {
			return new BigInteger(1, data).toString();
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new SequenceNumberMessage(new BigInteger(1, data).intValue());
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final byte[] data = BigInteger.valueOf(((SequenceNumberMessage) message).getSequence()).toByteArray();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final SequenceNumberMessage e = (SequenceNumberMessage) message;
			messageData.set("sequence", BigInteger.valueOf(e.getSequence()));
		}
	},
	TEXT(0x01) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new TextMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final TextMessage m = (TextMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final TextMessage m = (TextMessage) message;
			messageData.set("text", m.getText());
		}
	},
	COPYRIGHT(0x02) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new CopyrightMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final CopyrightMessage m = (CopyrightMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final CopyrightMessage m = (CopyrightMessage) message;
			messageData.set("text", m.getText());
		}
	},
	TRACK_NAME(0x03) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new TrackNameMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final TrackNameMessage m = (TrackNameMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final TrackNameMessage m = (TrackNameMessage) message;
			messageData.set("text", m.getText());
		}
	},
	INSTRUMENT_NAME(0x04) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new InstrumentNameMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final InstrumentNameMessage m = (InstrumentNameMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final InstrumentNameMessage m = (InstrumentNameMessage) message;
			messageData.set("text", m.getText());
		}
	},
	LYRICS(0x05) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new LyricsMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final LyricsMessage m = (LyricsMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final LyricsMessage m = (LyricsMessage) message;
			messageData.set("text", m.getText());
		}
	},
	MARKER_TEXT(0x06) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new MarkerTextMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final MarkerTextMessage m = (MarkerTextMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final MarkerTextMessage m = (MarkerTextMessage) message;
			messageData.set("text", m.getText());
		}
	},
	CUE_POINT(0x07) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new CuePointMessage(new String(data));
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final CuePointMessage m = (CuePointMessage) message;
			final byte[] data = m.getText().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final CuePointMessage m = (CuePointMessage) message;
			messageData.set("text", m.getText());
		}
	},
	CHANNEL_PREFIX(0x20) {
		@Override
		public String toString(final byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Message createMessage(final byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			// TODO Auto-generated method stub

		}
	},
	END_OF_TRACK(0x2F) {
		@Override
		public String toString(final byte[] data) {
			assert data.length == 0;
			return "";
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new EndOfTrackMessage();
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			return new MetaMessage(type, new byte[0], 0);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
		}
	},
	TEMPO(0x51) {
		@Override
		public String toString(final byte[] data) {
			assert data.length == 3;
			final BigInteger mpq = new BigInteger(1, data);
			final BigInteger bpm = BPM_MPQ_FACTOR.divide(mpq);
			return bpm + " bpm";
		}

		@Override
		public Message createMessage(final byte[] data) {
			final BigInteger mpq = new BigInteger(1, data);
			final BigInteger bpm = BPM_MPQ_FACTOR.divide(mpq);
			return new TempoMessage(bpm.intValueExact());
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final TempoMessage m = (TempoMessage) message;
			final BigInteger bpm = BigInteger.valueOf(m.getBpm());
			final BigInteger mpq = BPM_MPQ_FACTOR.divide(bpm);
			final byte[] data = mpq.toByteArray();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final TempoMessage m = (TempoMessage) message;
			messageData.set("bpm", BigInteger.valueOf(m.getBpm()));
		}
	},
	TIME_SIGNATURE(0x58) {
		@Override
		public String toString(final byte[] data) {
			assert data.length == 4;
			final byte n = data[0];
			final byte d = data[1];
			final byte m = data[2];
			final byte t = data[2];
			String result = n + "/" + (1 << d);
			if (m != 0) {
				result += ", metronome: every " + m + " ticks";
			}
			if (t != 0) {
				result += ", " + t + " ticks per quarter";
			}
			return result;
		}

		@Override
		public Message createMessage(final byte[] data) {
			final byte n = data[0];
			final byte d = data[1];
			final byte m = data[2];
			final byte t = data[2];
			return new TimeSignatureMessage(n, d, m, t);
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final TimeSignatureMessage m = (TimeSignatureMessage) message;
			final byte[] data = new byte[] { m.getNumerator(), m.getDenominator(), m.getMetronome(), m.getTicks() };
			return new MetaMessage(type, data, 4);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final TimeSignatureMessage m = (TimeSignatureMessage) message;
			messageData.set("numerator", BigInteger.valueOf(m.getNumerator()));
			messageData.set("denominator", BigInteger.valueOf(m.getDenominator()));
			messageData.set("metronome", BigInteger.valueOf(m.getMetronome()));
			messageData.set("ticks", BigInteger.valueOf(m.getTicks()));
		}
	},
	KEY_SIGNATURE(0x59) {
		@Override
		public String toString(final byte[] data) {
			assert data.length == 2;
			final byte keysig = data[0];
			final byte mode = data[1];

			switch (mode) {
			case 0: // major
				switch (keysig) {
				case -7:
					return "Cb";
				case -6:
					return "Gb";
				case -5:
					return "Db";
				case -4:
					return "Ab";
				case -3:
					return "Eb";
				case -2:
					return "Bb";
				case -1:
					return "F";
				case 0:
					return "C";
				case 1:
					return "G";
				case 2:
					return "D";
				case 3:
					return "A";
				case 4:
					return "E";
				case 5:
					return "B";
				case 6:
					return "F#";
				case 7:
					return "C#";
				}
				break;
			case 1: // minor
				switch (keysig) {
				case -7:
					return "Abm";
				case -6:
					return "Ebm";
				case -5:
					return "Bbm";
				case -4:
					return "Fm";
				case -3:
					return "Cm";
				case -2:
					return "Gm";
				case -1:
					return "Dm";
				case 0:
					return "Am";
				case 1:
					return "Em";
				case 2:
					return "Bm";
				case 3:
					return "F#m";
				case 4:
					return "C#m";
				case 5:
					return "G#m";
				case 6:
					return "D#m";
				case 7:
					return "A#m";
				}
				break;
			}

			return null;
		}

		@Override
		public Message createMessage(final byte[] data) {
			final byte keysig = data[0];
			final byte mode = data[1];
			return new KeySignatureMessage(keysig, mode);
		}

		@Override
		public MetaMessage createMidiMessage(final Message message) throws InvalidMidiDataException {
			final KeySignatureMessage m = (KeySignatureMessage) message;
			final byte[] data = { m.getKeysig(), m.getMode() };
			return new MetaMessage(type, data, 2);
		}

		@Override
		public void fill(final Dictionary messageData, final Message message) {
			final KeySignatureMessage m = (KeySignatureMessage) message;
			messageData.set("keysig", BigInteger.valueOf(m.getKeysig()));
			messageData.set("mode", BigInteger.valueOf(m.getMode()));
		}
	};

	private static final BigInteger BPM_MPQ_FACTOR = new BigInteger("60000000");
	private static final Map<Byte, MetaMessageType> MAP;
	static {
		final Map<Byte, MetaMessageType> m = new HashMap<>();
		for (final MetaMessageType type : values()) {
			m.put(type.type, type);
		}
		MAP = Collections.unmodifiableMap(m);
	}

	public final byte type;

	private MetaMessageType(final int type) {
		this.type = (byte) type;
	}

	public static MetaMessageType at(final int type) {
		return MAP.get((byte) type);
	}

	public abstract String toString(byte[] data);

	public abstract Message createMessage(byte[] data);

	public abstract MetaMessage createMidiMessage(Message message) throws InvalidMidiDataException;

	public abstract void fill(final Dictionary messageData, final Message message);
}
