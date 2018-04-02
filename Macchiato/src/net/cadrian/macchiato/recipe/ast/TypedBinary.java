package net.cadrian.macchiato.recipe.ast;

public class TypedBinary<T1, T2, R> extends Binary implements TypedExpression<R> {

	private final TypedExpression<? extends T1> leftOperand;
	private final TypedExpression<? extends T2> rightOperand;
	private final Class<? extends R> resultType;

	public TypedBinary(final TypedExpression<? extends T1> leftOperand, final Binary.Operator operator,
			final TypedExpression<? extends T2> rightOperand, final Class<? extends R> resultType) {
		super(operator);
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.resultType = resultType;
	}

	public TypedExpression<? extends T1> getLeftOperand() {
		return leftOperand;
	}

	public TypedExpression<? extends T2> getRightOperand() {
		return rightOperand;
	}

	@Override
	public int position() {
		return leftOperand.position();
	}

	@Override
	public <T> TypedExpression<T> typed(final Class<? extends T> type) {
		if (type.isAssignableFrom(resultType)) {
			@SuppressWarnings("unchecked")
			final TypedExpression<T> result = (TypedExpression<T>) this;
			return result;
		}
		return null;
	}

}
