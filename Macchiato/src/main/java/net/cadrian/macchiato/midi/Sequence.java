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

public class Sequence {

	private final Track[] tracks;

	public Sequence(final javax.sound.midi.Sequence midi) {
		final javax.sound.midi.Track[] midiTracks = midi.getTracks();
		this.tracks = new Track[midiTracks.length];
		for (int i = 0; i < midiTracks.length; i++) {
			this.tracks[i] = new Track(midiTracks[i]);
		}
	}

}
