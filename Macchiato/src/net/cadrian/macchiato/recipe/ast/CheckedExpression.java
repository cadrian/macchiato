package net.cadrian.macchiato.recipe.ast;

public class CheckedExpression<T> implements TypedExpression<T> {

	public static interface Visitor<T> extends Node.Visitor {
		void visit(CheckedExpression<T> e);
	}

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

	public Class<? extends T> getType() {
		return type;
	}

	public Expression getToCheck() {
		return toCheck;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void accept(final Node.Visitor v) {
		((Visitor<T>) v).visit(this);
	}

}
