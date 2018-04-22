package net.cadrian.macchiato.recipe.ast.expression;

public interface ExpressionVisitor extends CheckedExpression.Visitor, FunctionCall.Visitor, Identifier.Visitor,
		IndexedExpression.Visitor, ManifestArray.Visitor, ManifestDictionary.Visitor, ManifestNumeric.Visitor,
		ManifestRegex.Visitor, ManifestString.Visitor, Result.Visitor, TypedBinary.Visitor, TypedUnary.Visitor {

}
