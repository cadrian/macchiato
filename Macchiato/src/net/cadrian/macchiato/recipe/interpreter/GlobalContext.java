package net.cadrian.macchiato.recipe.interpreter;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;

class GlobalContext extends Context {

	private final Interpreter interpreter;
	private final Map<String, Object> global = new HashMap<>();
	private Track track;
	private AbstractEvent event;
	private boolean next;

	public GlobalContext(final Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	Interpreter getInterpreter() {
		return interpreter;
	}

	void setTrack(final int trackIndex, final javax.sound.midi.Track trackIn, final javax.sound.midi.Track trackOut) {
		this.track = new Track(trackIndex, trackIn, trackOut);
	}

	void setEvent(final int eventIndex, final long tick, final MetaMessageType type, final MetaMessage message) {
		this.event = new MetaEvent(eventIndex, tick, type, message);
	}

	void setEvent(final int eventIndex, final long tick, final ShortMessageType type, final ShortMessage message) {
		this.event = new ShortEvent(eventIndex, tick, type, message);
	}

	@Override
	void emit(final AbstractEvent event) {
		track.add(event);
	}

	@Override
	boolean isNext() {
		return next;
	}

	@Override
	void setNext(final boolean next) {
		this.next = next;
	}

	@Override
	Track getTrack() {
		return track;
	}

	@Override
	AbstractEvent getEvent() {
		return event;
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T get(final String key) {
		return (T) global.get(key);
	}

	@Override
	<T> T set(final String key, final T value) {
		return setGlobal(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T setGlobal(final String key, final T value) {
		return (T) global.put(key, value);
	}

}
