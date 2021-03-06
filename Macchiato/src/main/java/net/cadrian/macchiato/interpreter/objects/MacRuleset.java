package net.cadrian.macchiato.interpreter.objects;

import java.util.LinkedHashMap;
import java.util.Map;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.core.DefFunction;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedDef;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class MacRuleset implements MacObject {

	private static class RulesetMethod implements Method<MacRuleset> {

		private final DefFunction def;

		RulesetMethod(final LocalizedDef def) {
			this.def = new DefFunction(def);
		}

		@Override
		public Identifier name() {
			return def.name();
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return def.getArgTypes();
		}

		@Override
		public Identifier[] getArgNames() {
			return def.getArgNames();
		}

		@Override
		public Class<? extends MacObject> getResultType() {
			return def.getResultType();
		}

		@Override
		public Ruleset getRuleset() {
			return def.getRuleset();
		}

		@Override
		public Class<MacRuleset> getTargetType() {
			return MacRuleset.class;
		}

		@Override
		public void run(final MacRuleset target, final Context context, final Position position) {
			def.run(context, position);
		}

	};

	private final Ruleset ruleset;
	private final Map<Identifier, ReadOnlyField<MacRuleset, MacRuleset>> fields = new LinkedHashMap<>();
	private final Map<Identifier, Method<MacRuleset>> methods = new LinkedHashMap<>();

	public MacRuleset(final Ruleset ruleset) {
		this.ruleset = ruleset;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		Field<T, R> result = (Field<T, R>) fields.get(name);
		if (result == null) {
			final Ruleset importedRuleset = this.ruleset.getRuleset(name);
			if (importedRuleset != null) {
				final MacRuleset rs = new MacRuleset(importedRuleset);
				final ReadOnlyField<MacRuleset, MacRuleset> field = new ReadOnlyField<>(name, ruleset, MacRuleset.class,
						MacRuleset.class, rs);
				fields.put(name, field);
				result = (Field<T, R>) rs;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		Method<T> result = (Method<T>) methods.get(name);
		if (result == null) {
			final LocalizedDef def = this.ruleset.getDef(name);
			if (def != null) {
				final RulesetMethod method = new RulesetMethod(def);
				methods.put(name, method);
				result = (Method<T>) method;
			}
		}
		return result;
	}

	@Override
	public <T extends MacObject> T asIndexType(final Class<T> type) {
		if (type == getClass()) {
			return type.cast(this);
		}
		return null;
	}

	@Override
	public String toString() {
		return "{MacRuleset ruleset=" + ruleset + " fields=" + fields + " methods=" + methods + "}";
	}

}
