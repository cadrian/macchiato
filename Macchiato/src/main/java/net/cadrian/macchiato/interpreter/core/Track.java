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
package net.cadrian.macchiato.interpreter.core;

import javax.sound.midi.MidiMessage;

import net.cadrian.macchiato.interpreter.Event;

class Track {

	private final int index;
	private final javax.sound.midi.Track input;
	private final javax.sound.midi.Track output;

	public Track(final int index, final javax.sound.midi.Track input, final javax.sound.midi.Track output) {
		this.index = index;
		this.input = input;
		this.output = output;
	}

	public int getIndex() {
		return index;
	}

	public javax.sound.midi.Track getInput() {
		return input;
	}

	public <M extends MidiMessage> void add(final Event<M> event) {
		output.add(event.createMidiEvent());
	}

}
