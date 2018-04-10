package net.cadrian.macchiato.recipe.interpreter;

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

	public void add(final AbstractEvent event) {
		output.add(event.createMidiEvent());
	}

}
