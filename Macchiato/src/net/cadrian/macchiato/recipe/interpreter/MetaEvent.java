package net.cadrian.macchiato.recipe.interpreter;

import javax.sound.midi.MetaMessage;

import net.cadrian.macchiato.midi.MetaMessageType;

class MetaEvent extends AbstractEvent {

	private final MetaMessageType type;
	private final MetaMessage message;

	public MetaEvent(final int index, final long tick, final MetaMessageType type, final MetaMessage message) {
		super(index, tick);
		this.type = type;
		this.message = message;
	}

}
