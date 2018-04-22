package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.AbstractCall;
import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Node;

public class FunctionCall extends AbstractCall implements Expression {

	public static interface Visitor extends Node.Visitor {
		void visitFunctionCall(FunctionCall functionCall);
	}

	public FunctionCall(final int position, final String name) {
		super(position, name);
	}

	@Override
	public TypedExpression typed(final Class<?> type) {
		return new CheckedExpression(this, type);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitFunctionCall(this);
	}

}
