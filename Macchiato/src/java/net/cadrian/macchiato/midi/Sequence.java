package net.cadrian.macchiato.midi;

public class Sequence {

	private final Track[] tracks;

	public Sequence(final javax.sound.midi.Sequence midi) {
		final javax.sound.midi.Track[] tracks = midi.getTracks();
		this.tracks = new Track[tracks.length];
		for (int i = 0; i < tracks.length; i++) {
			this.tracks[i] = new Track(tracks[i]);
		}
	}

}
