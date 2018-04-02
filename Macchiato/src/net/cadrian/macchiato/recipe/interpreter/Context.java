package net.cadrian.macchiato.recipe.interpreter;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;

class Context {

	private Track track;
	private AbstractEvent event;
	private boolean next;

	void setTrack(final int trackIndex, final javax.sound.midi.Track track) {
		this.track = new Track(trackIndex, track);
	}

	void setEvent(final int eventIndex, final long tick, final MetaMessageType type, final MetaMessage message) {
		this.event = new MetaEvent(eventIndex, tick, type, message);
	}

	public void setEvent(final int eventIndex, final long tick, final ShortMessageType type,
			final ShortMessage message) {
		this.event = new ShortEvent(eventIndex, tick, type, message);
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(final boolean next) {
		this.next = next;
	}

}
