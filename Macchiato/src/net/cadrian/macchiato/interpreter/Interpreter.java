package net.cadrian.macchiato.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.recipe.ast.BoundFilter.Bound;
import net.cadrian.macchiato.recipe.ast.Filter;
import net.cadrian.macchiato.recipe.ast.Recipe;

public class Interpreter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Interpreter.class);

	final Recipe recipe;

	public Interpreter(final Recipe recipe) {
		this.recipe = recipe;
	}

	public void run() throws InvalidMidiDataException, IOException {
		run(System.in, System.out);
	}

	public void run(final InputStream in, final OutputStream out) throws InvalidMidiDataException, IOException {
		final Sequence sequenceIn = MidiSystem.getSequence(in);
		final Sequence sequenceOut = new Sequence(sequenceIn.getDivisionType(), sequenceIn.getResolution(),
				sequenceIn.getTracks().length);
		final GlobalContext context = new GlobalContext(this);
		final Track[] tracksIn = sequenceIn.getTracks();
		final Track[] tracksOut = sequenceOut.getTracks();
		filter(Bound.BEGIN_SEQUENCE, context);
		for (int trackIndex = 0; trackIndex < tracksIn.length; trackIndex++) {
			final Track trackIn = tracksIn[trackIndex];
			final Track trackOut = tracksOut[trackIndex];
			context.setTrack(trackIndex, trackIn, trackOut);
			filter(Bound.BEGIN_TRACK, context);
			for (int eventIndex = 0; eventIndex < trackIn.size(); eventIndex++) {
				final MidiEvent eventIn = trackIn.get(eventIndex);
				final BigInteger tick = BigInteger.valueOf(eventIn.getTick());
				final MidiMessage message = eventIn.getMessage();
				if (message instanceof MetaMessage) {
					// Meta message
					final MetaMessage metaMessage = (MetaMessage) message;
					final MetaMessageType type = MetaMessageType.at(metaMessage.getType());
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
					context.setEvent(tick, type, shortMessage);
					filter(context);
				} else {
					throw new RuntimeException("Unknown type of MIDI message: " + message.getClass());
				}
			}
			filter(Bound.END_TRACK, context);
		}
		filter(Bound.END_SEQUENCE, context);
		// TODO: determine file type from input
		MidiSystem.write(sequenceOut, 0, out);
	}

	private void filter(final Bound bound, final GlobalContext context) {
		LOGGER.debug("<-- {}", bound);
		context.setNext(false);
		final BoundFilterVisitor visitor = new BoundFilterVisitor(context, bound);
		for (final Filter filter : recipe.getFilters()) {
			filter.accept(visitor);
		}
		LOGGER.debug("-->");
	}

	private void filter(final GlobalContext context) {
		LOGGER.debug("<--");
		context.setNext(false);
		final ConditionFilterVisitor visitor = new ConditionFilterVisitor(context);
		for (final Filter filter : recipe.getFilters()) {
			filter.accept(visitor);
			if (context.isNext()) {
				return;
			}
		}
		context.emit();
		LOGGER.debug("-->");
	}

}
