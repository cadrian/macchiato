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

	private final Map<Long, List<Message<? extends MidiMessage>>> events = new TreeMap<>();

	public Track(final javax.sound.midi.Track midi) {
		for (int i = 0; i < midi.size(); i++) {
			final MidiEvent event = midi.get(i);
			final long tick = event.getTick();
			final MidiMessage message = event.getMessage();
			if (message instanceof MetaMessage) {
				final MetaMessage metaMessage = (MetaMessage) message;
				final MetaMessageType type = MetaMessageType.at(metaMessage.getType());
				final Message<MetaMessage> e = type.createMessage(metaMessage.getData());
				addEvent(tick, e);
			} else if (message instanceof SysexMessage) {
				// final SysexMessage sysexMessage = (SysexMessage) message;
				System.out.println("IGNORED: system extension message @" + tick);
			} else if (message instanceof ShortMessage) {
				final ShortMessage shortMessage = (ShortMessage) message;
				final ShortMessageType type = ShortMessageType.at(shortMessage.getCommand());
				final Message<ShortMessage> e = type.createMessage(shortMessage.getChannel(), shortMessage.getData1(),
						shortMessage.getData2());
				addEvent(tick, e);
			} else {
				assert false;
			}
		}
	}

	private void addEvent(final long tick, final Message<? extends MidiMessage> event) {
		final List<Message<? extends MidiMessage>> eventsInTick = events.get(tick);
		if (eventsInTick != null) {
			eventsInTick.add(event);
		} else {
			final List<Message<? extends MidiMessage>> newEventsInTick = new ArrayList<>();
			newEventsInTick.add(event);
			events.put(tick, newEventsInTick);
		}
	}

}
