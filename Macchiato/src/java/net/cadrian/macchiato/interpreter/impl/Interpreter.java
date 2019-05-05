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
package net.cadrian.macchiato.interpreter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.ruleset.ast.BoundFilter.Bound;
import net.cadrian.macchiato.ruleset.ast.Filter;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class Interpreter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Interpreter.class);

	private final Ruleset ruleset;

	public Interpreter(final Ruleset ruleset) {
		this.ruleset = ruleset;
	}

	public void run(final String[] args) throws InvalidMidiDataException, IOException {
		run(System.in, System.out, args);
	}

	public void run(final InputStream in, final OutputStream out, final String[] args)
			throws InvalidMidiDataException, IOException {
		final int inputMidiType;
		if (in.markSupported()) {
			in.mark(16);
			final MidiFileFormat midiFileFormat = MidiSystem.getMidiFileFormat(in);
			inputMidiType = midiFileFormat.getType();
			in.reset();
		} else {
			inputMidiType = -1;
		}

		final Sequence sequenceIn = MidiSystem.getSequence(in);
		final Sequence sequenceOut = new Sequence(sequenceIn.getDivisionType(), sequenceIn.getResolution(),
				sequenceIn.getTracks().length);
		final Track[] tracksIn = sequenceIn.getTracks();
		final Track[] tracksOut = sequenceOut.getTracks();

		final int outputMidiType;
		if (inputMidiType == -1) {
			if (tracksIn.length > 1) {
				LOGGER.warn("Cannot mark input stream, will emit MIDI file type 1");
				outputMidiType = 1;
			} else {
				LOGGER.warn("Cannot mark input stream, will emit MIDI file type 0");
				outputMidiType = 0;
			}
		} else {
			outputMidiType = inputMidiType;
			LOGGER.debug("Could read input MIDI file type, will emit MIDI file type {}", outputMidiType);
		}

		run(tracksIn, tracksOut, args);

		LOGGER.info("Writing out file");
		MidiSystem.write(sequenceOut, outputMidiType, out);

		LOGGER.info("Done.");
	}

	private void run(final Track[] tracksIn, final Track[] tracksOut, final String[] args) {
		final GlobalContext context = new GlobalContext(ruleset, args);

		final int tracksCount = tracksIn.length;

		LOGGER.info("Starting sequence ({} {})", tracksCount, tracksCount == 1 ? "track" : "tracks");
		filter(Bound.BEGIN_SEQUENCE, context);
		for (int trackIndex = 0; trackIndex < tracksCount; trackIndex++) {
			LOGGER.info("Starting track {}/{}", trackIndex + 1, tracksCount);
			final Track trackIn = tracksIn[trackIndex];
			final Track trackOut = tracksOut[trackIndex];
			context.setTrack(trackIndex, trackIn, trackOut);
			filter(Bound.BEGIN_TRACK, context);
			for (int eventIndex = 0; eventIndex < trackIn.size(); eventIndex++) {
				final MidiEvent eventIn = trackIn.get(eventIndex);
				final MacNumber tick = MacNumber.valueOf(eventIn.getTick());
				final MidiMessage message = eventIn.getMessage();
				if (message instanceof MetaMessage) {
					// Meta message
					final MetaMessage metaMessage = (MetaMessage) message;
					final MetaMessageType type = MetaMessageType.at(metaMessage.getType());
					if (type == null) {
						throw new RuntimeException(
								"Unknown type of MIDI meta message: 0x" + Integer.toHexString(metaMessage.getType()));
					}
					context.setEvent(tick, type, metaMessage);
					filter(context);
				} else if (message instanceof SysexMessage) {
					// System-exclusive message, ignored
					@SuppressWarnings("unused")
					final SysexMessage sysexMessage = (SysexMessage) message;
					trackOut.add(eventIn);
				} else if (message instanceof ShortMessage) {
					// According to javadoc, any other type of message
					final ShortMessage shortMessage = (ShortMessage) message;
					final ShortMessageType type = ShortMessageType.at(shortMessage.getCommand());
					if (type == null) {
						throw new RuntimeException("Unknown type of MIDI short message: 0x"
								+ Integer.toHexString(shortMessage.getCommand()));
					}
					context.setEvent(tick, type, shortMessage);
					filter(context);
				} else {
					throw new RuntimeException("Unknown type of MIDI message: " + message.getClass());
				}
			}
			filter(Bound.END_TRACK, context);
		}
		filter(Bound.END_SEQUENCE, context);
	}

	private void filter(final Bound bound, final GlobalContext context) {
		LOGGER.debug("<-- {}", bound);
		context.setNext(false);
		final BoundFilterVisitor visitor = new BoundFilterVisitor(context, bound);
		for (final Filter filter : ruleset.getFilters()) {
			filter.accept(visitor);
		}
		LOGGER.debug("-->");
	}

	private void filter(final GlobalContext context) {
		LOGGER.debug("<--");
		context.setNext(false);
		final ConditionFilterVisitor visitor = new ConditionFilterVisitor(context);
		for (final Filter filter : ruleset.getFilters()) {
			filter.accept(visitor);
			if (context.isNext()) {
				return;
			}
		}
		context.emit();
		LOGGER.debug("-->");
	}

}
