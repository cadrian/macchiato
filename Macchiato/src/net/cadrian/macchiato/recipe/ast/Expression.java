package net.cadrian.macchiato.recipe.ast;

public interface Expression extends Node {

	<T> TypedExpression<T> typed(Class<? extends T> type);

}
