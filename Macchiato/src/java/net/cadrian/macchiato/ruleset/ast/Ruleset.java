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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ruleset {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ruleset.class);

	public static class LocalizedDef {
		public final Def def;
		public final Ruleset ruleset;

		LocalizedDef(final Def def, final Ruleset ruleset) {
			this.def = def;
			this.ruleset = ruleset;
		}
	}

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

	public LocalizedDef getDef(final String name) {
		final LocalizedDef result;
		final Def def = defs.get(name);
		if (def == null) {
			result = null;
		} else {
			result = new LocalizedDef(def, this);
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

	public Ruleset getScope(final String name) {
		return scopes.get(name);
	}

	@Override
	public String toString() {
		return "{Ruleset defs=" + defs + " filters=" + filters + " scopes=" + scopes + "}";
	}

	public Ruleset simplify() {
		LOGGER.debug("<-- {}", this);
		Ruleset result = this;
		for (int simplifyCount = 256; simplifyCount-- > 0;) {
			LOGGER.debug("Simplify #{}", 256 - simplifyCount, result);
			boolean changed = false;
			final Ruleset simplifyRuleset = new Ruleset(position);
			for (final Map.Entry<String, Ruleset> scope : result.scopes.entrySet()) {
				final String scopeName = scope.getKey();
				LOGGER.debug("Simplify nested scope {}", scopeName);
				final Ruleset value = scope.getValue();
				final Ruleset simplifyValue = value.simplify();
				simplifyRuleset.addScope(scopeName, simplifyValue);
				changed |= simplifyValue != value;
			}
			for (final Def def : result.defs.values()) {
				LOGGER.debug("Simplify def {}", def.name());
				final Def simplifyDef = def.simplify();
				simplifyRuleset.addDef(simplifyDef);
				changed |= simplifyDef != def;
			}
			for (final Filter filter : result.filters) {
				LOGGER.debug("Simplify filter");
				final Filter simplifyFilter = filter.simplify();
				simplifyRuleset.addFilter(simplifyFilter);
				changed |= simplifyFilter != filter;
			}
			if (!changed) {
				LOGGER.debug("Found simplify fix point");
				break;
			}
			result = simplifyRuleset;
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

}
