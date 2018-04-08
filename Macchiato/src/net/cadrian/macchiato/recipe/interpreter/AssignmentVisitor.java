package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;

import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.expression.CheckedExpression;
import net.cadrian.macchiato.recipe.ast.expression.ExpressionVisitor;
import net.cadrian.macchiato.recipe.ast.expression.FunctionCall;
import net.cadrian.macchiato.recipe.ast.expression.Identifier;
import net.cadrian.macchiato.recipe.ast.expression.IndexedExpression;
import net.cadrian.macchiato.recipe.ast.expression.ManifestArray;
import net.cadrian.macchiato.recipe.ast.expression.ManifestDictionary;
import net.cadrian.macchiato.recipe.ast.expression.ManifestNumeric;
import net.cadrian.macchiato.recipe.ast.expression.ManifestRegex;
import net.cadrian.macchiato.recipe.ast.expression.ManifestString;
import net.cadrian.macchiato.recipe.ast.expression.Result;
import net.cadrian.macchiato.recipe.ast.expression.TypedBinary;
import net.cadrian.macchiato.recipe.ast.expression.TypedUnary;

class AssignmentVisitor implements ExpressionVisitor {

	@FunctionalInterface
	private static interface Setter {
		void set(Object value);
	}

	private final Context context;
	private Object value;
	private Setter setter;

	AssignmentVisitor(final Context context) {
		this.context = context;
	}

	void assign(final Expression target, final Object value) {
		this.value = null;
		setter = null;
		target.accept(this);
		setter.set(value);
	}

	@Override
	public void visit(final CheckedExpression e) {
		e.getToCheck().accept(this);
	}

	@Override
	public void visit(final FunctionCall functionCall) {
		throw new InterpreterException("Cannot assign to a function call");
	}

	@Override
	public void visit(final Identifier identifier) {
		final String key = identifier.getName();
		value = context.get(key);
		setter = (final Object value) -> {
			context.setGlobal(key, value);
		};
	}

	@Override
	public void visit(Result result) {
		value = context.get("result");
		setter = (final Object value) -> {
			context.set("result", value);
		};
	}

	@Override
	public void visit(final IndexedExpression indexedExpression) {
		indexedExpression.getIndexed().accept(this);
		final Object index = context.eval(indexedExpression.getIndex());
		if (index instanceof BigInteger) {
			if (value != null && !(value instanceof Array)) {
				throw new InterpreterException("invalid index");
			}
			final Array array = value == null ? new Array() : (Array) value;
			value = array;
			setter = (final Object value) -> {
				array.set((BigInteger) index, value);
			};
		} else if (index instanceof String) {
			if (value != null && !(value instanceof Dictionary)) {
				throw new InterpreterException("invalid index");
			}
			final Dictionary dictionary = value == null ? new Dictionary() : (Dictionary) value;
			value = dictionary;
			setter = (final Object value) -> {
				dictionary.set((String) index, value);
			};
		} else {
			throw new InterpreterException("Cannot use " + index.getClass().getSimpleName() + " as index");
		}
	}

	@Override
	public void visit(final ManifestArray array) {
		throw new InterpreterException("Invalid left-side assignment");
	}

	@Override
	public void visit(final ManifestDictionary dictionary) {
		throw new InterpreterException("Invalid left-side assignment");
	}

	@Override
	public void visit(final ManifestNumeric manifestNumeric) {
		throw new InterpreterException("Invalid left-side assignment");
	}

	@Override
	public void visit(final ManifestRegex manifestRegex) {
		throw new InterpreterException("Invalid left-side assignment");
	}

	@Override
	public void visit(final ManifestString manifestString) {
		throw new InterpreterException("Invalid left-side assignment");
	}

	@Override
	public void visit(final TypedBinary typedBinary) {
		throw new InterpreterException("Invalid left-side assignment");
	}

	@Override
	public void visit(final TypedUnary typedUnary) {
		throw new InterpreterException("Invalid left-side assignment");
	}

}
