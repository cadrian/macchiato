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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Array implements Container<BigInteger> {

	private static final ThreadLocal<Set<Array>> TO_STRING_GATE = new ThreadLocal<Set<Array>>() {
		@Override
		protected Set<Array> initialValue() {
			return new HashSet<>();
		}
	};

	private final Map<BigInteger, Object> array = new TreeMap<>();

	@Override
	public Object set(final BigInteger index, final Object value) {
		return array.put(index, value);
	}

	@Override
	public Object get(final BigInteger index) {
		return array.get(index);
	}

	@Override
	public String toString() {
		final Set<Array> gate = TO_STRING_GATE.get();
		if (gate.contains(this)) {
			return "RECURSIVE ARRAY";
		}

		gate.add(this);
		try {
			final StringBuilder result = new StringBuilder();
			result.append('[');
			for (final Map.Entry<BigInteger, Object> entry : array.entrySet()) {
				if (result.length() > 1) {
					result.append(", ");
				}
				result.append(entry.getKey()).append('=').append(entry.getValue());
			}
			result.append(']');
			return result.toString();
		} finally {
			gate.remove(this);
		}
	}

}
