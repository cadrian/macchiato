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
package net.cadrian.macchiato.interpreter.impl;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MidiMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Event;
import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

class LocalContext extends Context {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalContext.class);

	private final Ruleset ruleset;
	private final Context parent;
	private final Map<String, Object> local = new HashMap<>();

	public LocalContext(final Context parent, final Ruleset ruleset) {
		this.parent = parent;
		this.ruleset = ruleset;
	}

	@Override
	Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public MacArray getArguments() {
		return parent.getArguments();
	}

	@Override
	boolean isNext() {
		return parent.isNext();
	}

	@Override
	void setNext(final boolean next) {
		parent.setNext(next);
	}

	@Override
	<M extends MidiMessage> Event<M> getEvent() {
		return parent.getEvent();
	}

	@Override
	<M extends MidiMessage> void emit(final Message<M> message, final MacNumber tick) {
		parent.emit(message, tick);
	}

	@Override
	protected Function getUncachedFunction(final String name) {
		final Function result = super.getUncachedFunction(name);
		if (result == null) {
			return parent.getFunction(name);
		}
		return result;
	}

	@Override
	public boolean has(final String key) {
		if (local.containsKey(key)) {
			return true;
		}
		return parent.has(key);
	}

	@Override
	public <T extends MacObject> T get(final String key) {
		LOGGER.debug("<-- {}", key);
		T result;
		if (local.containsKey(key)) {
			@SuppressWarnings("unchecked")
			final T old = (T) local.get(key);
			result = old;
		} else {
			result = parent.get(key);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	public <T extends MacObject> T set(final String key, final T value) {
		LOGGER.debug("<-- {} = {}", key, value);
		T result;
		if (local.containsKey(key)) {
			@SuppressWarnings("unchecked")
			final T old = (T) local.put(key, value);
			result = old;
		} else {
			result = parent.set(key, value);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	void declareLocal(final String name) {
		LOGGER.debug("<-- {}", name);
		if (!local.containsKey(name)) {
			local.put(name, null);
		}
		LOGGER.debug("-->");
	}

}
