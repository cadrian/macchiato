package net.cadrian.macchiato.recipe.ast;

import java.util.regex.Pattern;

public class ManifestRegex implements TypedExpression<Pattern> {

	private final Pattern value;
	private final int position;

	public ManifestRegex(final int position, final Pattern value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(Pattern.class)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return null;
	}

	public Pattern getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

}
