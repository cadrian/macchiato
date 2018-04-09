package net.cadrian.macchiato.midi;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.cadrian.macchiato.midi.event.metaev.CopyrightEvent;
import net.cadrian.macchiato.midi.event.metaev.CuePointEvent;
import net.cadrian.macchiato.midi.event.metaev.EndOfTrackEvent;
import net.cadrian.macchiato.midi.event.metaev.InstrumentNameEvent;
import net.cadrian.macchiato.midi.event.metaev.KeySignatureEvent;
import net.cadrian.macchiato.midi.event.metaev.LyricsEvent;
import net.cadrian.macchiato.midi.event.metaev.MarkerTextEvent;
import net.cadrian.macchiato.midi.event.metaev.SequenceNumberEvent;
import net.cadrian.macchiato.midi.event.metaev.TempoEvent;
import net.cadrian.macchiato.midi.event.metaev.TextEvent;
import net.cadrian.macchiato.midi.event.metaev.TimeSignatureEvent;
import net.cadrian.macchiato.midi.event.metaev.TrackNameEvent;
import net.cadrian.macchiato.recipe.interpreter.Dictionary;

public enum MetaMessageType {
	SEQUENCE_NUMBER(0x00) {
		@Override
		public String toString(final byte[] data) {
			return new BigInteger(1, data).toString();
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new SequenceNumberEvent(new BigInteger(1, data).intValue());
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final SequenceNumberEvent e = (SequenceNumberEvent) event;
			eventData.set("sequence", BigInteger.valueOf(e.getSequence()));
		}
	},
	TEXT(0x01) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new TextEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final TextEvent e = (TextEvent) event;
			eventData.set("text", e.getText());
		}
	},
	COPYRIGHT(0x02) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new CopyrightEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final CopyrightEvent e = (CopyrightEvent) event;
			eventData.set("text", e.getText());
		}
	},
	TRACK_NAME(0x03) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new TrackNameEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final TrackNameEvent e = (TrackNameEvent) event;
			eventData.set("text", e.getText());
		}
	},
	INSTRUMENT_NAME(0x04) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new InstrumentNameEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final InstrumentNameEvent e = (InstrumentNameEvent) event;
			eventData.set("text", e.getText());
		}
	},
	LYRICS(0x05) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new LyricsEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final LyricsEvent e = (LyricsEvent) event;
			eventData.set("text", e.getText());
		}
	},
	MARKER_TEXT(0x06) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new MarkerTextEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final MarkerTextEvent e = (MarkerTextEvent) event;
			eventData.set("text", e.getText());
		}
	},
	CUE_POINT(0x07) {
		@Override
		public String toString(final byte[] data) {
			return new String(data);
		}

		@Override
		public Event createEvent(final byte[] data) {
			return new CuePointEvent(new String(data));
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final CuePointEvent e = (CuePointEvent) event;
			eventData.set("text", e.getText());
		}
	},
	CHANNEL_PREFIX(0x20) {
		@Override
		public String toString(final byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Event createEvent(final byte[] data) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
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
		public Event createEvent(final byte[] data) {
			return new EndOfTrackEvent();
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
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
		public Event createEvent(final byte[] data) {
			final BigInteger mpq = new BigInteger(1, data);
			final BigInteger bpm = BPM_MPQ_FACTOR.divide(mpq);
			return new TempoEvent(bpm.intValueExact());
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final TempoEvent e = (TempoEvent) event;
			eventData.set("bpm", BigInteger.valueOf(e.getBpm()));
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
		public Event createEvent(final byte[] data) {
			final byte n = data[0];
			final byte d = data[1];
			final byte m = data[2];
			final byte t = data[2];
			return new TimeSignatureEvent(n, d, m, t);
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final TimeSignatureEvent e = (TimeSignatureEvent) event;
			eventData.set("numerator", BigInteger.valueOf(e.getNumerator()));
			eventData.set("denominator", BigInteger.valueOf(e.getDenominator()));
			eventData.set("metronome", BigInteger.valueOf(e.getMetronome()));
			eventData.set("ticks", BigInteger.valueOf(e.getTicks()));
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
		public Event createEvent(final byte[] data) {
			final byte keysig = data[0];
			final byte mode = data[1];
			return new KeySignatureEvent(keysig, mode);
		}

		@Override
		public void fill(final Dictionary eventData, final Event event) {
			final KeySignatureEvent e = (KeySignatureEvent) event;
			eventData.set("keysig", BigInteger.valueOf(e.getKeysig()));
			eventData.set("mode", BigInteger.valueOf(e.getMode()));
		}
	};

	private static final BigInteger BPM_MPQ_FACTOR = new BigInteger("60000000");
	private static final Map<Byte, MetaMessageType> MAP;
	static {
		final Map<Byte, MetaMessageType> m = new HashMap<>();
		for (final MetaMessageType type : values()) {
			m.put(type.status, type);
		}
		MAP = Collections.unmodifiableMap(m);
	}

	public final byte status;

	private MetaMessageType(final int status) {
		this.status = (byte) status;
	}

	public static MetaMessageType at(final int status) {
		return MAP.get((byte) status);
	}

	public abstract String toString(byte[] data);

	public abstract Event createEvent(byte[] data);

	public abstract void fill(final Dictionary eventData, final Event event);
}
