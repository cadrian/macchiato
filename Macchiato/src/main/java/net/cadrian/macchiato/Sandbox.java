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
package net.cadrian.macchiato;

import java.io.File;

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

public final class Sandbox {

	public static void main(final String[] args) throws Exception {
		final Sequence sequence = MidiSystem.getSequence(new File(args[0]));
		final Track[] tracks = sequence.getTracks();
		for (final Track track : tracks) {
			System.out.println("** Track **");
			for (int i = 0; i < track.size(); i++) {
				final MidiEvent event = track.get(i);
				final long tick = event.getTick();
				final MidiMessage message = event.getMessage();
				if (message instanceof MetaMessage) {
					// Meta message
					final MetaMessage metaMessage = (MetaMessage) message;
					final MetaMessageType type = MetaMessageType.at(metaMessage.getType());
					System.out.println(
							"@" + tick + "\tMETA  " + type + " (" + type.toString(metaMessage.getData()) + ")");
				} else if (message instanceof SysexMessage) {
					// System-exclusive message
					final SysexMessage sysexMessage = (SysexMessage) message;
					System.out.println("@" + tick + "\tSYSEX (" + new String(sysexMessage.getData()) + ")");
				} else if (message instanceof ShortMessage) {
					// According to javadoc, any other type of message
					final ShortMessage shortMessage = (ShortMessage) message;
					final ShortMessageType type = ShortMessageType.at(shortMessage.getCommand());
					if (type == null) {
						System.out.println("@" + tick + "\tSHORT channel " + shortMessage.getChannel() + ": " + "0x"
								+ Integer.toString(shortMessage.getCommand(), 16));
					} else {
						System.out.println("@" + tick + "\tSHORT channel " + shortMessage.getChannel() + ": " + type
								+ " (" + type.toString(shortMessage.getData1(), shortMessage.getData2()) + ")");
					}
				} else {
					throw new IllegalStateException("unknown type of message: " + message.getClass());
				}
			}
		}
	}

	private Sandbox() {
		throw new IllegalStateException("No instance");
	}

}
