package net.cadrian.macchiato.recipe.ast;

public class IndexedExpression implements Expression {

	private final Expression indexed;
	private final TypedExpression<Comparable<?>> index;

	public IndexedExpression(final Expression indexed, final TypedExpression<Comparable<?>> index) {
		this.indexed = indexed;
		this.index = index;
	}

	@Override
	public int position() {
		return indexed.position();
	}

	@Override
	public <T> TypedExpression<T> typed(Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
	}

}
