package net.cadrian.macchiato.recipe.ast;

import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;

public interface Expression extends Node {

	<T> TypedExpression<T> typed(Class<? extends T> type);

}
