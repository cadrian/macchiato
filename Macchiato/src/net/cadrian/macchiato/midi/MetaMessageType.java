package net.cadrian.macchiato.midi;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

import net.cadrian.macchiato.interpreter.Dictionary;
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

public enum MetaMessageType {
	SEQUENCE_NUMBER(0x00) {
		@Override
		public String toString(final byte[] data) {
			return new BigInteger(1, data).toString();
		}

		@Override
		public Message createMessage(final byte[] data) {
			return new SequenceNumberMessage(new BigInteger(1, data).intValueExact());
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_SEQUENCE;
		}

		@Override
		public Message create(final Object... args) {
			final BigInteger sequence = (BigInteger) args[0];
			return new SequenceNumberMessage(sequence.intValueExact());
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new TextMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new CopyrightMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new TrackNameMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new InstrumentNameMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new LyricsMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new MarkerTextMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message create(final Object... args) {
			final String text = (String) args[0];
			return new CuePointMessage(text);
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

		@Override
		public Class<?>[] getArgTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getArgNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Message create(final Object... args) {
			// TODO Auto-generated method stub
			return null;
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_0;
		}

		@Override
		public String[] getArgNames() {
			return ARG_0;
		}

		@Override
		public Message create(final Object... args) {
			return new EndOfTrackMessage();
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT1;
		}

		@Override
		public String[] getArgNames() {
			return ARG_BPM;
		}

		@Override
		public Message create(final Object... args) {
			final BigInteger mpq = (BigInteger) args[0];
			return new TempoMessage(mpq.intValueExact());
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT4;
		}

		@Override
		public String[] getArgNames() {
			return ARG_TIMESIG;
		}

		@Override
		public Message create(final Object... args) {
			final BigInteger numerator = (BigInteger) args[0];
			final BigInteger denominator = (BigInteger) args[1];
			final BigInteger metronome = (BigInteger) args[2];
			final BigInteger ticks = (BigInteger) args[3];
			return new TimeSignatureMessage(numerator.byteValueExact(), denominator.byteValueExact(),
					metronome.byteValueExact(), ticks.byteValueExact());
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

		@Override
		public Class<?>[] getArgTypes() {
			return TYPE_INT2;
		}

		@Override
		public String[] getArgNames() {
			return ARG_KEYSIG;
		}

		@Override
		public Message create(final Object... args) {
			final BigInteger keysig = (BigInteger) args[0];
			final BigInteger mode = (BigInteger) args[1];
			return new KeySignatureMessage(keysig.byteValueExact(), mode.byteValueExact());
		}
	};

	private static final String[] ARG_KEYSIG = new String[] { "keysig", "mode" };
	private static final String[] ARG_TIMESIG = new String[] { "numerator", "denominator", "metronome", "ticks" };
	private static final String[] ARG_BPM = new String[] { "bpm" };
	private static final String[] ARG_0 = new String[0];
	private static final Class<?>[] TYPE_0 = new Class<?>[0];
	private static final String[] ARG_TEXT = new String[] { "text" };
	private static final Class<?>[] TYPE_STR1 = new Class<?>[] { String.class };
	private static final String[] ARG_SEQUENCE = new String[] { "sequence" };
	private static final Class<?>[] TYPE_INT1 = new Class<?>[] { BigInteger.class };
	private static final Class<?>[] TYPE_INT2 = new Class<?>[] { BigInteger.class, BigInteger.class };
	private static final Class<?>[] TYPE_INT4 = new Class<?>[] { BigInteger.class, BigInteger.class, BigInteger.class,
			BigInteger.class };
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

	public abstract Class<?>[] getArgTypes();

	public abstract String[] getArgNames();

	public abstract Message create(Object... args);
}
