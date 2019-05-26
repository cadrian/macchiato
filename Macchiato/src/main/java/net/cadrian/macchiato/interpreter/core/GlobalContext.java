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
package net.cadrian.macchiato.interpreter.core;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.ContractException;
import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.Identifiers;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.event.MetaEvent;
import net.cadrian.macchiato.interpreter.event.ShortEvent;
import net.cadrian.macchiato.interpreter.functions.natfun.Native;
import net.cadrian.macchiato.interpreter.objects.MacEvent;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacRuleset;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.midi.ControlChange;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.midi.MetaMessageType;
import net.cadrian.macchiato.midi.ShortMessageType;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

class GlobalContext extends Context {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalContext.class);
	private final Ruleset ruleset;
	private final Map<Identifier, MacObject> global = new LinkedHashMap<>();
	private final Map<Identifier, Function> nativeFunctions = new LinkedHashMap<>();
	private Track track;
	private Event<? extends MidiMessage> event;
	private boolean next;

	public GlobalContext(final Ruleset ruleset, final String[] args) {
		this.ruleset = ruleset;

		final MacArray arguments = new MacArray();
		for (int i = 0; i < args.length; i++) {
			arguments.set(MacNumber.valueOf(i), MacString.valueOf(args[i]));
		}
		global.put(new Identifier("arguments", Position.NONE), arguments);

		for (final MetaMessageType type : MetaMessageType.values()) {
			global.put(new Identifier(type.name(), Position.NONE), type);
			nativeFunctions.put(new Identifier(type.name(), Position.NONE),
					new MetaMessageCreationFunction(type, ruleset));
		}
		for (final ShortMessageType type : ShortMessageType.values()) {
			global.put(new Identifier(type.name(), Position.NONE), type);
			nativeFunctions.put(new Identifier(type.name(), Position.NONE),
					new ShortMessageCreationFunction(type, ruleset));
		}
		for (final ControlChange mpc : ControlChange.values()) {
			global.put(new Identifier(mpc.name(), Position.NONE), mpc);
		}
		for (final Native fun : Native.values()) {
			final Function function = fun.getFunction(ruleset);
			nativeFunctions.put(function.name(), function);
		}
	}

	@Override
	Ruleset getRuleset() {
		return ruleset;
	}

	void setTrack(final int trackIndex, final javax.sound.midi.Track trackIn, final javax.sound.midi.Track trackOut) {
		this.track = new Track(trackIndex, trackIn, trackOut);
	}

	void setEvent(final MacNumber tick, final MetaMessageType type, final MetaMessage message) {
		final MetaEvent metaEvent = new MetaEvent(tick, type, message);
		final MacEvent eventData = new MacEvent(ruleset, tick, type);
		type.fill(eventData, metaEvent.createMessage());
		LOGGER.debug("Setting meta event {}", eventData);
		global.put(Identifiers.EVENT, eventData);
		this.event = metaEvent;
	}

	void setEvent(final MacNumber tick, final ShortMessageType type, final ShortMessage message) {
		final ShortEvent shortEvent = new ShortEvent(tick, type, message);
		final MacEvent eventData = new MacEvent(ruleset, tick, type);
		type.fill(eventData, shortEvent.createMessage());
		LOGGER.debug("Setting short event {}", eventData);
		global.put(Identifiers.EVENT, eventData);
		this.event = shortEvent;
	}

	@Override
	<M extends MidiMessage> void emit(final Message<M> message, final MacNumber tick) {
		LOGGER.debug("Emitting message {} at {}", message, tick);
		track.add(message.toEvent(tick.getValue()));
	}

	@Override
	boolean isNext() {
		return next;
	}

	@Override
	void setNext(final boolean next) {
		this.next = next;
	}

	@SuppressWarnings("unchecked")
	@Override
	<M extends MidiMessage> Event<M> getEvent() {
		return (Event<M>) event;
	}

	@Override
	protected Function getUncachedFunction(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		Function result = super.getUncachedFunction(name);
		if (result == null) {
			result = nativeFunctions.get(name);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	public boolean has(final Identifier key) {
		return global.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> T get(final Identifier key) {
		LOGGER.debug("<-- {}", key);
		T result = (T) global.get(key);
		if (result == null) {
			final Ruleset r = ruleset.getScope(key);
			if (r != null) {
				final MacRuleset newRuleset = new MacRuleset(r);
				global.put(key, newRuleset);
				result = (T) newRuleset;
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	public <T extends MacObject> T set(final Identifier key, final T value) {
		LOGGER.debug("<-- {} = {}", key, value);
		@SuppressWarnings("unchecked")
		final T result = (T) global.put(key, value);
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	void declareLocal(final Identifier name) {
		throw new InterpreterException("BUG: unexpected local declaration in global context", name.position());
	}

	@Override
	public void evaluateOldData(final Expression ensures) {
		throw new InterpreterException("BUG: unexpected old data evaluation in global context", ensures.position());
	}

	@Override
	public boolean checkContract(final Expression contract, final String tag) throws ContractException {
		throw new InterpreterException("BUG: unexpected contract checking in global context", contract.position());
	}

}
