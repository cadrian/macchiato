package net.cadrian.macchiato.recipe.ast.expression;

import net.cadrian.macchiato.recipe.ast.Expression;

public abstract interface TypedExpression extends Expression {

	public Class<?> getType();

}