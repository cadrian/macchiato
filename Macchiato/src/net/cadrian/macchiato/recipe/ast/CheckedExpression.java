package net.cadrian.macchiato.recipe.ast;

public class CheckedExpression<T> implements TypedExpression<T> {

	private final Class<? extends T> type;
	private final Expression toCheck;

	public CheckedExpression(final Expression toCheck, final Class<? extends T> type) {
		this.toCheck = toCheck;
		this.type = type;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(toCheck, type);
	}

	@Override
	public int position() {
		return toCheck.position();
	}

}
