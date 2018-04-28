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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ruleset {

	private final Map<String, Def> defs = new HashMap<>();
	private final List<Filter> filters = new ArrayList<>();
	private final Map<String, Ruleset> scopes = new LinkedHashMap<>();
	private final int position;

	private List<Filter> filtersCache;

	public Ruleset(final int position) {
		this.position = position;
	}

	public int position() {
		return position;
	}

	public Def addDef(final Def def) {
		return defs.put(def.name(), def);
	}

	public void addFilter(final Filter filter) {
		filtersCache = null;
		filters.add(filter);
	}

	public Def getDef(final String name) {
		Def result = defs.get(name);
		if (result == null) {
			final int i = name.indexOf('.');
			if (i != -1) {
				final Ruleset scope = scopes.get(name.substring(0, i));
				if (scope != null) {
					result = scope.getDef(name.substring(i + 1));
				}
			}
		}
		return result;
	}

	public List<Filter> getFilters() {
		if (filtersCache == null) {
			final List<Filter> filters = new ArrayList<>();
			getFiltersIn(filters);
			filtersCache = Collections.unmodifiableList(filters);
		}
		return filtersCache;
	}

	private void getFiltersIn(final List<Filter> filters) {
		for (final Ruleset scope : scopes.values()) {
			scope.getFiltersIn(filters);
		}
		filters.addAll(this.filters);
	}

	public Ruleset addScope(final String name, final Ruleset scope) {
		filtersCache = null;
		return scopes.put(name, scope);
	}

	public boolean hasScope(final String name) {
		return scopes.containsKey(name);
	}

	@Override
	public String toString() {
		return "{Ruleset defs=" + defs + " filters=" + filters + " scopes=" + scopes + "}";
	}

}
