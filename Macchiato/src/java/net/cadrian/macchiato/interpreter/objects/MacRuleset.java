package net.cadrian.macchiato.interpreter.objects;

import java.util.HashMap;
import java.util.Map;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.interpreter.impl.DefFunction;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedDef;

public class MacRuleset implements MacObject {

	private static class RulesetMethod implements Method<MacRuleset> {

		private final DefFunction def;

		RulesetMethod(final LocalizedDef def) {
			this.def = new DefFunction(def);
		}

		@Override
		public String name() {
			return def.name();
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return def.getArgTypes();
		}

		@Override
		public String[] getArgNames() {
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
		public void run(final MacRuleset target, final Context context, final int position) {
			def.run(context, position);
		}

	};

	private final Ruleset ruleset;
	private final Map<String, ReadOnlyField<MacRuleset, MacRuleset>> fields = new HashMap<>();
	private final Map<String, RulesetMethod> methods = new HashMap<>();

	public MacRuleset(final Ruleset ruleset) {
		this.ruleset = ruleset;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset, final String name) {
		if (Character.isLowerCase(name.charAt(0))) {
			return null;
		}
		Field<T, R> result = (Field<T, R>) fields.get(name);
		if (result == null) {
			final Ruleset scope = this.ruleset.getScope(name);
			if (scope != null) {
				final MacRuleset rs = new MacRuleset(scope);
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
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final String name) {
		if (Character.isLowerCase(name.charAt(0))) {
			return null;
		}
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

}
