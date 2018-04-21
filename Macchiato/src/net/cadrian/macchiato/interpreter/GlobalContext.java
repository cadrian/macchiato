package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.recipe.ast.Def;

class GlobalContext extends Context {

	private final Interpreter interpreter;
	private final Map<String, Object> global = new HashMap<>();
	private final Map<String, Function> nativeFunctions = new HashMap<>();
	private final Map<String, Function> functions = new HashMap<>();
	private Track track;
	private AbstractEvent event;
	private boolean next;

	public GlobalContext(final Interpreter interpreter) {
		this.interpreter = interpreter;
		for (final MetaMessageType type : MetaMessageType.values()) {
			global.put(type.name(), type);
			nativeFunctions.put(type.name(), new MetaMessageCreationFunction(type));
		}
		for (final ShortMessageType type : ShortMessageType.values()) {
			global.put(type.name(), type);
			nativeFunctions.put(type.name(), new ShortMessageCreationFunction(type));
		}
		for (final ControlChange mpc : ControlChange.values()) {
			global.put(mpc.name(), mpc);
		}
		final RandomFunction randomFunction = new RandomFunction();
		nativeFunctions.put(randomFunction.name(), randomFunction);
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
		eventData.set("tick", tick);
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

	@Override
	Function getFunction(final String name) {
		final Function fn = functions.get(name);
		if (fn != null) {
			return fn;
		}
		final Function result;
		final Def def = interpreter.recipe.getDef(name);
		if (def == null) {
			result = nativeFunctions.get(name);
		} else {
			result = new DefFunction(def);
		}
		functions.put(name, result);
		return result;
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
