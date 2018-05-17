/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.container.Dictionary;
import net.cadrian.macchiato.interpreter.natfun.Native;
import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

class GlobalContext extends Context {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalContext.class);

	private final Interpreter interpreter;
	private final Map<String, Object> global = new HashMap<>();
	private final Map<String, Function> nativeFunctions = new HashMap<>();
	private final Ruleset ruleset;
	private Track track;
	private AbstractEvent<? extends MidiMessage> event;
	private boolean next;

	public GlobalContext(final Interpreter interpreter, final Ruleset ruleset) {
		this.interpreter = interpreter;
		this.ruleset = ruleset;
		for (final MetaMessageType type : MetaMessageType.values()) {
			global.put(type.name(), type);
			nativeFunctions.put(type.name(), new MetaMessageCreationFunction(type, ruleset));
		}
		for (final ShortMessageType type : ShortMessageType.values()) {
			global.put(type.name(), type);
			nativeFunctions.put(type.name(), new ShortMessageCreationFunction(type, ruleset));
		}
		for (final ControlChange mpc : ControlChange.values()) {
			global.put(mpc.name(), mpc);
		}
		for (final Native fun : Native.values()) {
			final Function function = fun.getFunction(ruleset);
			nativeFunctions.put(function.name(), function);
		}
	}

	@Override
	Interpreter getInterpreter() {
		return interpreter;
	}

	@Override
	Ruleset getRuleset() {
		return ruleset;
	}

	void setTrack(final int trackIndex, final javax.sound.midi.Track trackIn, final javax.sound.midi.Track trackOut) {
		this.track = new Track(trackIndex, trackIn, trackOut);
	}

	void setEvent(final BigInteger tick, final MetaMessageType type, final MetaMessage message) {
		final MetaEvent metaEvent = new MetaEvent(tick, type, message);
		final Dictionary eventData = new Dictionary();
		eventData.set("type", type);
		eventData.set("tick", tick);
		type.fill(eventData, metaEvent.createMessage());
		LOGGER.debug("Setting meta event {}", eventData);
		global.put("event", eventData);
		this.event = metaEvent;
	}

	void setEvent(final BigInteger tick, final ShortMessageType type, final ShortMessage message) {
		final ShortEvent shortEvent = new ShortEvent(tick, type, message);
		final Dictionary eventData = new Dictionary();
		eventData.set("type", type);
		eventData.set("tick", tick);
		type.fill(eventData, shortEvent.createMessage());
		LOGGER.debug("Setting short event {}", eventData);
		global.put("event", eventData);
		this.event = shortEvent;
	}

	@Override
	<M extends MidiMessage> void emit(final Message<M> message, final BigInteger tick) {
		LOGGER.debug("Emitting message {} at {}", message, tick);
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

	@SuppressWarnings("unchecked")
	@Override
	<M extends MidiMessage> AbstractEvent<M> getEvent() {
		return (AbstractEvent<M>) event;
	}

	@Override
	protected Function getUncachedFunction(final String name) {
		LOGGER.debug("<-- {}", name);
		Function result = super.getUncachedFunction(name);
		if (result == null) {
			result = nativeFunctions.get(name);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	public boolean has(final String key) {
		return global.containsKey(key);
	}

	@Override
	public <T> T get(final String key) {
		LOGGER.debug("<-- {}", key);
		@SuppressWarnings("unchecked")
		final T result = (T) global.get(key);
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	public <T> T set(final String key, final T value) {
		LOGGER.debug("<-- {} = {}", key, value);
		@SuppressWarnings("unchecked")
		final T result = (T) global.put(key, value);
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	void declareLocal(final String name) {
		throw new InterpreterException("BUG: unexpected local declaration in global context", 0);
	}

}
