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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dictionary implements Container<String> {

	private static final ThreadLocal<Set<Dictionary>> TO_STRING_GATE = new ThreadLocal<Set<Dictionary>>() {
		@Override
		protected Set<Dictionary> initialValue() {
			return new HashSet<>();
		}
	};

	private final Map<String, Object> dictionary = new HashMap<>();

	@Override
	public Object set(final String index, final Object value) {
		return dictionary.put(index, value);
	}

	@Override
	public Object get(final String index) {
		return dictionary.get(index);
	}

	@Override
	public String toString() {
		final Set<Dictionary> gate = TO_STRING_GATE.get();
		if (gate.contains(this)) {
			return "RECURSIVE DICTIONARY";
		}

		gate.add(this);
		try {
			final StringBuilder result = new StringBuilder();
			result.append('{');
			for (final Map.Entry<String, Object> entry : dictionary.entrySet()) {
				if (result.length() > 1) {
					result.append(", ");
				}
				result.append(entry.getKey()).append('=').append(entry.getValue());
			}
			result.append('}');
			return result.toString();
		} finally {
			gate.remove(this);
		}
	}

}
