package net.cadrian.macchiato.recipe.ast;

public class TypedUnary<T, R> extends Unary implements TypedExpression<R> {

	public static interface Visitor<T, R> extends Node.Visitor {
		void visit(TypedUnary<T, R> typedUnary);
	}

	private final TypedExpression<? extends T> operand;
	private final Class<? extends R> resultType;
	private final int position;

	public TypedUnary(final int position, final Unary.Operator operator, final TypedExpression<? extends T> operand,
			final Class<? extends R> resultType) {
		super(operator);
		this.position = position;
		this.operand = operand;
		this.resultType = resultType;
	}

	public TypedExpression<? extends T> getOperand() {
		return operand;
	}

	@Override
	public int position() {
		return position;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(resultType)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void accept(final Node.Visitor v) {
		((Visitor<T, R>) v).visit(this);
	}

}
