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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Ruleset {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ruleset.class);

	private final Map<Identifier, Def> defs = new LinkedHashMap<>();
	private final Map<Identifier, Clazz> clazzes = new LinkedHashMap<>();
	private final List<Filter> filters = new ArrayList<>();
	private final Map<Identifier, Ruleset> rulesets = new LinkedHashMap<>();
	private final String path;
	private final Position position; // position of import in parent ruleset

	private List<Filter> filtersCache;

	public static class LocalizedDef {
		// non-null if it is a constructor
		public final Clazz clazz;

		// non-null if it is a normal def or if the clazz defines a constructor
		public final Def def;

		// always non-null
		public final Ruleset ruleset;

		LocalizedDef(final Clazz clazz, final Def def, final Ruleset ruleset) {
			this.clazz = clazz;
			this.def = def;
			this.ruleset = ruleset;
		}
	}

	public static class LocalizedClazz {
		public final Clazz clazz;
		public final Ruleset ruleset;

		LocalizedClazz(final Clazz clazz, final Ruleset ruleset) {
			this.clazz = clazz;
			this.ruleset = ruleset;
		}
	}

	public Ruleset(final Position position, final String path) {
		LOGGER.debug("NEW RULESET: {}", path);
		this.path = path;
		this.position = position;
	}

	public Position position() {
		return position;
	}

	public Def addDef(final Def def) {
		LOGGER.debug("adding def {} to ruleset {}", def, path);
		return defs.put(def.name(), def);
	}

	public Clazz addClazz(final Clazz clazz) {
		LOGGER.debug("adding class {} to ruleset {}", clazz, path);
		return clazzes.put(clazz.name(), clazz);
	}

	public void addFilter(final Filter filter) {
		filtersCache = null;
		filters.add(filter);
	}

	public LocalizedDef getDef(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		final LocalizedDef result;
		final Def def = defs.get(name);
		if (def == null) {
			final Clazz clazz = clazzes.get(name);
			if (clazz == null) {
				LOGGER.debug("def {} not found in ruleset {}", name, path);
				result = null;
			} else {
				LOGGER.debug("def {} is a constructor in ruleset {}", name, path);
				result = new LocalizedDef(clazz, clazz.getDef(name), this);
			}
		} else {
			result = new LocalizedDef(null, def, this);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	public LocalizedClazz getClazz(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		final LocalizedClazz result;
		final Clazz clazz = clazzes.get(name);
		if (clazz == null) {
			LOGGER.debug("class {} not found in ruleset {}", name, path);
			result = null;
		} else {
			result = new LocalizedClazz(clazz, this);
		}
		LOGGER.debug("--> {}", result);
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
		for (final Ruleset ruleset : rulesets.values()) {
			ruleset.getFiltersIn(filters);
		}
		filters.addAll(this.filters);
	}

	public Ruleset addRuleset(final Identifier name, final Ruleset ruleset) {
		filtersCache = null;
		return rulesets.put(name, ruleset);
	}

	public boolean hasRuleset(final Identifier name) {
		return rulesets.containsKey(name);
	}

	public Ruleset getRuleset(final Identifier name) {
		LOGGER.debug("<-- {}", name);
		LOGGER.debug("rulesets={}", rulesets);
		final Ruleset result = rulesets.get(name);
		LOGGER.debug("--> {}", result);
		return result;
	}

	@Override
	public String toString() {
		return "{Ruleset " + path + " clazzes=" + clazzes + " defs=" + defs + " filters=" + filters + " rulesets="
				+ rulesets + "}";
	}

	public Ruleset simplify() {
		LOGGER.debug("<-- {}", this);
		Ruleset result = this;
		for (int simplifyCount = 1; simplifyCount < 256; simplifyCount++) {
			LOGGER.debug("Simplify #{}: {}", simplifyCount, result);
			boolean changed = false;
			final Ruleset simplifyRuleset = new Ruleset(position, path);
			for (final Map.Entry<Identifier, Ruleset> entry : result.rulesets.entrySet()) {
				final Identifier rulesetName = entry.getKey();
				LOGGER.debug("Simplify nested ruleset {}", rulesetName);
				final Ruleset value = entry.getValue();
				final Ruleset simplifyValue = value.simplify();
				simplifyRuleset.addRuleset(rulesetName, simplifyValue);
				changed |= !simplifyValue.equals(value);
			}
			for (final Def def : result.defs.values()) {
				LOGGER.debug("Simplify def {}", def.name());
				final Def simplifyDef = def.simplify();
				simplifyRuleset.addDef(simplifyDef);
				changed |= !simplifyDef.equals(def);
			}
			for (final Clazz clazz : result.clazzes.values()) {
				LOGGER.debug("Simplify class {}", clazz.name());
				final Clazz simplifyClazz = clazz.simplify();
				simplifyRuleset.addClazz(simplifyClazz);
				changed |= !simplifyClazz.equals(clazz);
			}
			for (final Filter filter : result.filters) {
				LOGGER.debug("Simplify filter");
				final Filter simplifyFilter = filter.simplify();
				simplifyRuleset.addFilter(simplifyFilter);
				changed |= !simplifyFilter.equals(filter);
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
