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

	private final Ruleset ruleset;
	private final Map<Identifier, ReadOnlyField<MacRuleset, MacRuleset>> fields = new LinkedHashMap<>();
	private final Map<Identifier, Method<MacRuleset>> methods = new LinkedHashMap<>();

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

	}

	public MacRuleset(final Ruleset ruleset) {
		this.ruleset = ruleset;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset,
			final Identifier name) {
		return (Field<T, R>) fields.computeIfAbsent(name, n -> {
			final Ruleset importedRuleset = this.ruleset.getRuleset(n);
			if (importedRuleset != null) {
				final MacRuleset rs = new MacRuleset(importedRuleset);
				return new ReadOnlyField<>(n, ruleset, MacRuleset.class, MacRuleset.class, rs);
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final Identifier name) {
		return (Method<T>) methods.computeIfAbsent(name, n -> {
			final LocalizedDef def = this.ruleset.getDef(name);
			if (def != null) {
				return new RulesetMethod(def);
			}
			return null;
		});
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
