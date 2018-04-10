package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Message;
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
		for (final MetaMessageType type : MetaMessageType.values()) {
			global.put(type.name(), type);
		}
		for (final ShortMessageType type : ShortMessageType.values()) {
			global.put(type.name(), type);
		}
		for (final ControlChange mpc : ControlChange.values()) {
			global.put(mpc.name(), mpc);
		}
	}

	@Override
	Interpreter getInterpreter() {
		return interpreter;
	}

	void setTrack(final int trackIndex, final javax.sound.midi.Track trackIn, final javax.sound.midi.Track trackOut) {
		this.track = new Track(trackIndex, trackIn, trackOut);
	}

	void setEvent(final BigInteger tick, final MetaMessageType type, final MetaMessage message) {
		this.event = new MetaEvent(tick, type, message);
		final Dictionary eventData = new Dictionary();
		eventData.set("type", type);
		type.fill(eventData, event.createMessage());
		global.put("event", eventData);
	}

	void setEvent(final BigInteger tick, final ShortMessageType type, final ShortMessage message) {
		this.event = new ShortEvent(tick, type, message);
		final Dictionary eventData = new Dictionary();
		eventData.set("type", type);
		type.fill(eventData, event.createMessage());
		global.put("event", eventData);
	}

	@Override
	void emit(final Message message, final BigInteger tick) {
		track.add(message.toEvent(tick));
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
