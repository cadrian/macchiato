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
package net.cadrian.macchiato.ruleset.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ruleset {

	private final Map<String, Def> defs = new HashMap<>();
	private final List<Filter> filters = new ArrayList<>();

	public Def addDef(final Def def) {
		return defs.put(def.name(), def);
	}

	public void addFilter(final Filter filter) {
		filters.add(filter);
	}

	public Def getDef(final String name) {
		return defs.get(name);
	}

	public List<Filter> getFilters() {
		return Collections.unmodifiableList(filters);
	}

	@Override
	public String toString() {
		return "{Ruleset defs=" + defs + " filters=" + filters + "}";
	}

}
