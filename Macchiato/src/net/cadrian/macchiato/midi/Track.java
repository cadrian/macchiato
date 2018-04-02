package net.cadrian.macchiato.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

public class Track {

	private final Map<Long, List<Event>> events = new TreeMap<>();

	public Track(final javax.sound.midi.Track midi) {
		for (int i = 0; i < midi.size(); i++) {
			final MidiEvent event = midi.get(i);
			final long tick = event.getTick();
			final MidiMessage message = event.getMessage();
			if (message instanceof MetaMessage) {
				final MetaMessage metaMessage = (MetaMessage) message;
				final MetaMessageType type = MetaMessageType.at(metaMessage.getType());
				final Event e = type.createEvent(metaMessage.getData());
				addEvent(tick, e);
			} else if (message instanceof SysexMessage) {
				// final SysexMessage sysexMessage = (SysexMessage) message;
				System.out.println("IGNORED: system extension message @" + tick);
			} else if (message instanceof ShortMessage) {
				final ShortMessage shortMessage = (ShortMessage) message;
				final ShortMessageType type = ShortMessageType.at(shortMessage.getCommand());
				final Event e = type.createEvent(shortMessage.getChannel(), shortMessage.getData1(),
						shortMessage.getData2());
				addEvent(tick, e);
			} else {
				assert false;
			}
		}
	}

	private void addEvent(final long tick, final Event event) {
		final List<Event> eventsInTick = events.get(tick);
		if (eventsInTick != null) {
			eventsInTick.add(event);
		} else {
			final List<Event> newEventsInTick = new ArrayList<>();
			newEventsInTick.add(event);
			events.put(tick, newEventsInTick);
		}
	}

}
