package net.cadrian.macchiato.recipe.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recipe {

	private final Map<String, Def> defs = new HashMap<>();
	private final List<Filter> filters = new ArrayList<>();

	public Def addDef(final Def def) {
		return defs.put(def.name(), def);
	}

	public void addFilter(final Filter filter) {
		filters.add(filter);
	}

	public Collection<Filter> getFilters() {
		return filters;
	}

}
