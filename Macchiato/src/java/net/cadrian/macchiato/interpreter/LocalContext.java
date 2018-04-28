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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.midi.Message;

class LocalContext extends Context {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalContext.class);

	private final Context parent;
	private final Map<String, Object> local = new HashMap<>();

	public LocalContext(final Context parent) {
		this.parent = parent;
	}

	@Override
	Interpreter getInterpreter() {
		return parent.getInterpreter();
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
	Track getTrack() {
		return parent.getTrack();
	}

	@Override
	AbstractEvent getEvent() {
		return parent.getEvent();
	}

	@Override
	void emit(final Message message, final BigInteger tick) {
		parent.emit(message, tick);
	}

	@Override
	Function getFunction(final String name) {
		return parent.getFunction(name);
	}

	@Override
	public boolean has(final String key) {
		if (local.containsKey(key)) {
			return true;
		}
		return parent.has(key);
	}

	@Override
	public <T> T get(final String key) {
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
	public <T> T set(final String key, final T value) {
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
