package net.cadrian.macchiato.interpreter.objects;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.impl.Context;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

class ReadOnlyField<T extends MacObject, R extends MacObject> implements Field<T, R> {

	private final String name;
	private final Ruleset ruleset;
	private final Class<T> targetType;
	private final Class<R> resultType;

	private final R value;

	ReadOnlyField(final String name, final Ruleset ruleset, final Class<T> targetType, final Class<R> resultType,
			final R initialValue) {
		this.name = name;
		this.ruleset = ruleset;
		this.targetType = targetType;
		this.resultType = resultType;
		this.value = initialValue;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Class<R> getResultType() {
		return resultType;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Class<T> getTargetType() {
		return targetType;
	}

	@Override
	public R get(final T target, final Context context, final int position) {
		return value;
	}

	@Override
	public R set(final T target, final Context context, final int position, final R newValue) {
		throw new InterpreterException("Field " + name + " cannot be set", position);
	}

}
