package net.cadrian.macchiato.recipe.interpreter;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.recipe.ast.Recipe;

public class Interpreter {

	private final Recipe recipe;

	public Interpreter(final Recipe recipe) {
		this.recipe = recipe;
	}

	public void run() throws InvalidMidiDataException, IOException {
		final Sequence sequence = MidiSystem.getSequence(System.in);
		final Context context = new Context();
		final Track[] tracks = sequence.getTracks();
		for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
			final Track track = tracks[trackIndex];
			context.setTrack(trackIndex, track);
			for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
				final MidiEvent event = track.get(eventIndex);
				final long tick = event.getTick();
				final MidiMessage message = event.getMessage();
				if (message instanceof MetaMessage) {
					// Meta message
					final MetaMessage metaMessage = (MetaMessage) message;
					final MetaMessageType type = MetaMessageType.at(metaMessage.getType());
					context.setEvent(eventIndex, tick, type, metaMessage);
					filter(context);
				} else if (message instanceof SysexMessage) {
					// System-exclusive message, ignored
					@SuppressWarnings("unused")
					final SysexMessage sysexMessage = (SysexMessage) message;
				} else if (message instanceof ShortMessage) {
					// According to javadoc, any other type of message
					final ShortMessage shortMessage = (ShortMessage) message;
					final ShortMessageType type = ShortMessageType.at(shortMessage.getCommand());
					context.setEvent(eventIndex, tick, type, shortMessage);
					filter(context);
				} else {
					throw new RuntimeException("unknown type of MIDI message: " + message.getClass());
				}
			}
		}
	}

	private void filter(final Context context) {
		// TODO Auto-generated method stub

	}

}
