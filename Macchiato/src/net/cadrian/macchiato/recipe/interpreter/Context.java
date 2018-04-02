package net.cadrian.macchiato.recipe.interpreter;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;

class Context {

	private Track track;
	private AbstractEvent event;

	void setTrack(int trackIndex, javax.sound.midi.Track track) {
		this.track = new Track(trackIndex, track);
	}

	void setEvent(int eventIndex, long tick, MetaMessageType type, MetaMessage message) {
		this.event = new MetaEvent(eventIndex, tick, type, message);
	}

	public void setEvent(int eventIndex, long tick, ShortMessageType type, ShortMessage message) {
		this.event = new ShortEvent(eventIndex, tick, type, message);		
	}

}
