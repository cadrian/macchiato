package net.cadrian.macchiato.recipe.interpreter;

import javax.sound.midi.MidiEvent;

import net.cadrian.macchiato.midi.Event;

public abstract class AbstractEvent {

	private final int index;
	private final long tick;

	public AbstractEvent(final int index, final long tick) {
		this.index = index;
		this.tick = tick;
	}

	public int getIndex() {
		return index;
	}

	public long getTick() {
		return tick;
	}

	public abstract Event asEvent();

	public abstract MidiEvent asMidi();

}
