package net.cadrian.macchiato.recipe.ast;

public class ManifestString implements TypedExpression<String> {

	private final String value;
	private final int position;

	public ManifestString(final int position, final String value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(String.class)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return null;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

}
