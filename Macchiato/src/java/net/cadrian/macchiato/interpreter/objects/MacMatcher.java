package net.cadrian.macchiato.interpreter.objects;

import java.util.regex.Matcher;

import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacMatcher implements MacObject {

	private static class MatchesMethod extends AbstractMethod<MacMatcher> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[0];
		private static final String[] ARG_NAMES = {};

		protected MatchesMethod(final Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacMatcher> getTargetType() {
			return MacMatcher.class;
		}

		@Override
		public void run(final MacMatcher target, final Context context, final int position) {
			final MacBoolean result = MacBoolean.valueOf(target.value.matches());
			context.set("result", result);
		}

		@Override
		public String name() {
			return "matches";
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return ARG_TYPES;
		}

		@Override
		public String[] getArgNames() {
			return ARG_NAMES;
		}

		@Override
		public Class<MacBoolean> getResultType() {
			return MacBoolean.class;
		}

	};

	private static class GroupMethod extends AbstractMethod<MacMatcher> {

		@SuppressWarnings("unchecked")
		private static final Class<? extends MacObject>[] ARG_TYPES = new Class[] { MacComparable.class };
		private static final String[] ARG_NAMES = { "group" };

		protected GroupMethod(Ruleset ruleset) {
			super(ruleset);
		}

		@Override
		public Class<MacMatcher> getTargetType() {
			return MacMatcher.class;
		}

		@Override
		public void run(MacMatcher target, Context context, int position) {
			final MacString result;
			MacComparable<?> group = context.get("group");
			if (group instanceof MacNumber) {
				MacNumber index = (MacNumber) group;
				result = MacString.valueOf(target.value.group(index.getValue().intValueExact()));
			} else if (group instanceof MacString) {
				MacString name = (MacString) group;
				result = MacString.valueOf(target.value.group(name.getValue()));
			} else {
				throw new InterpreterException("invalid group value: must be a number or a string", position);
			}
			context.set("result", result);
		}

		@Override
		public String name() {
			return "group";
		}

		@Override
		public Class<? extends MacObject>[] getArgTypes() {
			return ARG_TYPES;
		}

		@Override
		public String[] getArgNames() {
			return ARG_NAMES;
		}

		@Override
		public Class<MacString> getResultType() {
			return MacString.class;
		}

	};

	private final Matcher value;

	public MacMatcher(final Matcher value) {
		this.value = value;
	}

	@Override
	public Method<? extends MacObject> getMethod(final Ruleset ruleset, final String name) {
		switch (name) {
		case "matches":
			return new MatchesMethod(ruleset);
		case "group":
			return new GroupMethod(ruleset);
		}
		return null;
	}

}
