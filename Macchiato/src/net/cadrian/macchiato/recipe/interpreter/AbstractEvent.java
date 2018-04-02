package net.cadrian.macchiato.recipe.interpreter;

class AbstractEvent {

	private final int index;
	private final long tick;

	public AbstractEvent(final int index, final long tick) {
		this.index = index;
		this.tick = tick;
	}

}
