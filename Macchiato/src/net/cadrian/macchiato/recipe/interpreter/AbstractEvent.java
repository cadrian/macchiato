package net.cadrian.macchiato.recipe.interpreter;

public class AbstractEvent {

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

}
