package net.cadrian.macchiato.recipe.interpreter;

class Track {

	private final int index;
	private final javax.sound.midi.Track midi;

	public Track(final int index, final javax.sound.midi.Track midi) {
		this.index = index;
		this.midi = midi;
	}

}
