package net.cadrian.macchiato.recipe.ast;

public class IndexedExpression implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visit(IndexedExpression indexedExpression);
	}

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
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		return new CheckedExpression<T>(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
