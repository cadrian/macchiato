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

import net.cadrian.macchiato.midi.Message;

class LocalContext extends Context {

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
	<T> T get(final String key) {
		@SuppressWarnings("unchecked")
		T result = (T) local.get(key);
		if (result == null) {
			result = parent.get(key);
		}
		return result;
	}

	@Override
	<T> T set(final String key, final T value) {
		@SuppressWarnings("unchecked")
		T result = (T) local.put(key, value);
		if (result == null) {
			result = parent.get(key);
		}
		return result;
	}

	@Override
	<T> T setGlobal(final String key, final T value) {
		return parent.setGlobal(key, value);
	}

}
