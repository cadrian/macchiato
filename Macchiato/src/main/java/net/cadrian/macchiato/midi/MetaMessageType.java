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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.objects.MacComparable;
import net.cadrian.macchiato.interpreter.objects.MacEvent;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.midi.message.m.CopyrightMessage;
import net.cadrian.macchiato.midi.message.m.CuePointMessage;
import net.cadrian.macchiato.midi.message.m.EndOfTrackMessage;
import net.cadrian.macchiato.midi.message.m.InstrumentNameMessage;
import net.cadrian.macchiato.midi.message.m.KeySignatureMessage;
import net.cadrian.macchiato.midi.message.m.LyricsMessage;
import net.cadrian.macchiato.midi.message.m.MarkerTextMessage;
import net.cadrian.macchiato.midi.message.m.ModulationMessage;
import net.cadrian.macchiato.midi.message.m.SequenceNumberMessage;
import net.cadrian.macchiato.midi.message.m.TempoMessage;
import net.cadrian.macchiato.midi.message.m.TextMessage;
import net.cadrian.macchiato.midi.message.m.TimeSignatureMessage;
import net.cadrian.macchiato.midi.message.m.TrackNameMessage;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public enum MetaMessageType implements MacComparable<MetaMessageType> {
	SEQUENCE_NUMBER(0x00) {
		@Override
		public String toString(final byte[] data) {
			return new BigInteger(1, data).toString();
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new SequenceNumberMessage(new BigInteger(1, data).intValueExact());
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final byte[] data = BigInteger.valueOf(((SequenceNumberMessage) message).getSequence()).toByteArray();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final SequenceNumberMessage e = (SequenceNumberMessage) message;
			messageData.addField(FIELD_SEQUENCE, MacNumber.valueOf(e.getSequence()));
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_INT1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_SEQUENCE;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacNumber sequence = (MacNumber) args[0];
			return new SequenceNumberMessage(sequence.getValue().intValueExact());
		}
	},
	TEXT(0x01) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new TextMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final TextMessage m = (TextMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final TextMessage m = (TextMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
			return new TextMessage(text);
		}
	},
	COPYRIGHT(0x02) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new CopyrightMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final CopyrightMessage m = (CopyrightMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final CopyrightMessage m = (CopyrightMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
			return new CopyrightMessage(text);
		}
	},
	TRACK_NAME(0x03) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new TrackNameMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final TrackNameMessage m = (TrackNameMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final TrackNameMessage m = (TrackNameMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
			return new TrackNameMessage(text);
		}
	},
	INSTRUMENT_NAME(0x04) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new InstrumentNameMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final InstrumentNameMessage m = (InstrumentNameMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final InstrumentNameMessage m = (InstrumentNameMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
			return new InstrumentNameMessage(text);
		}
	},
	LYRICS(0x05) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new LyricsMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final LyricsMessage m = (LyricsMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final LyricsMessage m = (LyricsMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
			return new LyricsMessage(text);
		}
	},
	MARKER_TEXT(0x06) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new MarkerTextMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final MarkerTextMessage m = (MarkerTextMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final MarkerTextMessage m = (MarkerTextMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
			return new MarkerTextMessage(text);
		}
	},
	CUE_POINT(0x07) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new CuePointMessage(MacString.valueOf(new String(data)));
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final CuePointMessage m = (CuePointMessage) message;
			final byte[] data = m.getText().getValue().getBytes();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final CuePointMessage m = (CuePointMessage) message;
			messageData.addField(FIELD_TEXT, m.getText());
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_STR1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TEXT;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacString text = (MacString) args[0];
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
		public Message<MetaMessage> createMessage(final byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			// TODO Auto-generated method stub

		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Identifier[] getArgNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	MODULATION(0x21) {
		@Override
		public String toString(final byte[] data) {
			return new BigInteger(1, data).toString();
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new ModulationMessage(new BigInteger(1, data).intValueExact());
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final byte[] data = MacNumber.valueOf(((ModulationMessage) message).getValue()).getValue().toByteArray();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final ModulationMessage e = (ModulationMessage) message;
			messageData.addField(FIELD_VALUE, MacNumber.valueOf(e.getValue()));
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_INT1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_VALUE;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacNumber value = (MacNumber) args[0];
			return new ModulationMessage(value.getValue().intValueExact());
		}
	},
	END_OF_TRACK(0x2F) {
		@Override
		public String toString(final byte[] data) {
			if (data != null && data.length != 0) {
				throw new IllegalArgumentException("data should be empty");
			}
			return "";
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			return new EndOfTrackMessage();
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			return new MetaMessage(type, new byte[0], 0);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			// nothing
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_0;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_0;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			return new EndOfTrackMessage();
		}
	},
	TEMPO(0x51) {
		@Override
		public String toString(final byte[] data) {
			if (data.length != 3) {
				throw new IllegalArgumentException("invalid data: expected 3 bytes");
			}
			final BigInteger mpq = new BigInteger(1, data);
			final BigInteger bpm = BPM_MPQ_FACTOR.divide(mpq);
			return bpm + " bpm";
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			final BigInteger mpq = new BigInteger(1, data);
			final BigInteger bpm = BPM_MPQ_FACTOR.divide(mpq);
			return new TempoMessage(bpm.intValueExact());
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final TempoMessage m = (TempoMessage) message;
			final BigInteger bpm = BigInteger.valueOf(m.getBpm());
			final BigInteger mpq = BPM_MPQ_FACTOR.divide(bpm);
			final byte[] data = mpq.toByteArray();
			return new MetaMessage(type, data, data.length);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final TempoMessage m = (TempoMessage) message;
			messageData.addField(FIELD_BPM, MacNumber.valueOf(m.getBpm()));
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_INT1;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_BPM;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacNumber mpq = (MacNumber) args[0];
			return new TempoMessage(mpq.getValue().intValueExact());
		}
	},
	TIME_SIGNATURE(0x58) {
		@Override
		public String toString(final byte[] data) {
			if (data.length != 4) {
				throw new IllegalArgumentException("invalid data: expected 4 bytes");
			}
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
		public Message<MetaMessage> createMessage(final byte[] data) {
			final byte n = data[0];
			final byte d = data[1];
			final byte m = data[2];
			final byte t = data[2];
			return new TimeSignatureMessage(n, d, m, t);
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final TimeSignatureMessage m = (TimeSignatureMessage) message;
			final byte[] data = new byte[] { m.getNumerator(), m.getDenominator(), m.getMetronome(), m.getTicks() };
			return new MetaMessage(type, data, 4);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final TimeSignatureMessage m = (TimeSignatureMessage) message;
			messageData.addField(FIELD_NUMERATOR, MacNumber.valueOf(m.getNumerator()));
			messageData.addField(FIELD_DENOMINATOR, MacNumber.valueOf(m.getDenominator()));
			messageData.addField(FIELD_METRONOME, MacNumber.valueOf(m.getMetronome()));
			messageData.addField(FIELD_TICKS, MacNumber.valueOf(m.getTicks()));
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_INT4;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_TIMESIG;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacNumber numerator = (MacNumber) args[0];
			final MacNumber denominator = (MacNumber) args[1];
			final MacNumber metronome = (MacNumber) args[2];
			final MacNumber ticks = (MacNumber) args[3];
			return new TimeSignatureMessage(numerator.getValue().byteValueExact(),
					denominator.getValue().byteValueExact(), metronome.getValue().byteValueExact(),
					ticks.getValue().byteValueExact());
		}
	},
	KEY_SIGNATURE(0x59) {
		@Override
		public String toString(final byte[] data) {
			if (data.length != 2) {
				throw new IllegalArgumentException("invalid data: expected 2 bytes");
			}
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
				default:
					throw new IllegalArgumentException("invalid data: unknown keysig");
				}
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
				default:
					throw new IllegalArgumentException("invalid data: unknown keysig");
				}
			default:
				throw new IllegalArgumentException("invalid data: unknown mode");
			}
		}

		@Override
		public Message<MetaMessage> createMessage(final byte[] data) {
			final byte keysig = data[0];
			final byte mode = data[1];
			return new KeySignatureMessage(keysig, mode);
		}

		@Override
		public MetaMessage createMidiMessage(final Message<MetaMessage> message) throws InvalidMidiDataException {
			final KeySignatureMessage m = (KeySignatureMessage) message;
			final byte[] data = { m.getKeysig(), m.getMode() };
			return new MetaMessage(type, data, 2);
		}

		@Override
		public void fill(final MacEvent messageData, final Message<MetaMessage> message) {
			final KeySignatureMessage m = (KeySignatureMessage) message;
			messageData.addField(FIELD_KEYSIG, MacNumber.valueOf(m.getKeysig()));
			messageData.addField(FIELD_MODE, MacNumber.valueOf(m.getMode()));
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return TYPE_INT2;
		}

		@Override
		public Identifier[] getArgNames() {
			return ARG_KEYSIG;
		}

		@Override
		public Message<MetaMessage> create(final MacObject... args) {
			final MacNumber keysig = (MacNumber) args[0];
			final MacNumber mode = (MacNumber) args[1];
			return new KeySignatureMessage(keysig.getValue().byteValueExact(), mode.getValue().byteValueExact());
		}
	};

	private static final Identifier FIELD_SEQUENCE = new Identifier("Sequence", Position.NONE);
	private static final Identifier FIELD_TEXT = new Identifier("Text", Position.NONE);
	private static final Identifier FIELD_VALUE = new Identifier("Value", Position.NONE);
	private static final Identifier FIELD_BPM = new Identifier("Bpm", Position.NONE);
	private static final Identifier FIELD_NUMERATOR = new Identifier("Numerator", Position.NONE);
	private static final Identifier FIELD_DENOMINATOR = new Identifier("Denominator", Position.NONE);
	private static final Identifier FIELD_METRONOME = new Identifier("Metronome", Position.NONE);
	private static final Identifier FIELD_TICKS = new Identifier("Ticks", Position.NONE);
	private static final Identifier FIELD_KEYSIG = new Identifier("KeySig", Position.NONE);
	private static final Identifier FIELD_MODE = new Identifier("Mode", Position.NONE);

	private static final Identifier[] ARG_KEYSIG = new Identifier[] { FIELD_KEYSIG, FIELD_MODE };
	private static final Identifier[] ARG_TIMESIG = new Identifier[] { FIELD_NUMERATOR, FIELD_DENOMINATOR,
			FIELD_METRONOME, FIELD_TICKS };
	private static final Identifier[] ARG_BPM = new Identifier[] { FIELD_BPM };
	private static final Identifier[] ARG_0 = {};
	private static final Identifier[] ARG_TEXT = new Identifier[] { FIELD_TEXT };
	private static final Identifier[] ARG_SEQUENCE = new Identifier[] { FIELD_SEQUENCE };
	private static final Identifier[] ARG_VALUE = new Identifier[] { FIELD_VALUE };

	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] TYPE_0 = new Class[0];
	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] TYPE_STR1 = new Class[] { MacString.class };
	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] TYPE_INT1 = new Class[] { MacNumber.class };
	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] TYPE_INT2 = new Class[] { MacNumber.class, MacNumber.class };
	@SuppressWarnings("unchecked")
	private static final Class<? extends MacObject>[] TYPE_INT4 = new Class[] { MacNumber.class, MacNumber.class,
			MacNumber.class, MacNumber.class };
	private static final BigInteger BPM_MPQ_FACTOR = BigInteger.valueOf(60_000_000L);
	private static final Map<Byte, MetaMessageType> MAP;
	static {
		final Map<Byte, MetaMessageType> m = new LinkedHashMap<>();
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

	public abstract Message<MetaMessage> createMessage(byte[] data);

	public abstract MetaMessage createMidiMessage(Message<MetaMessage> message) throws InvalidMidiDataException;

	public abstract void fill(final MacEvent messageData, final Message<MetaMessage> message);

	public abstract Class<? extends MacObject>[] getArgTypes();

	public abstract Identifier[] getArgNames();

	public abstract Message<MetaMessage> create(MacObject... args);

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return null;
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		return null;
	}

	@Override
	public <T extends MacObject> T asIndexType(final Class<T> type) {
		if (type == getClass()) {
			return type.cast(this);
		}
		if (type == MacNumber.class) {
			return type.cast(MacNumber.valueOf(this.type));
		}
		if (type == MacString.class) {
			return type.cast(MacString.valueOf(name()));
		}
		return null;
	}

}
