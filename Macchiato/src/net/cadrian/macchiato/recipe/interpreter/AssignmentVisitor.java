package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
import net.cadrian.macchiato.recipe.ast.expression.TypedBinary;
import net.cadrian.macchiato.recipe.ast.expression.TypedUnary;

class AssignmentVisitor implements ExpressionVisitor {

	@FunctionalInterface
	private static interface Setter {
		void set(Object value);
	}

	private final Context context;
	private final Map<String, Object> global;
	private Object value;
	private Setter setter;

	AssignmentVisitor(final Context context) {
		this.context = context;
		this.global = context.global;
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
		value = global.get(key);
		setter = (final Object value) -> {
			global.put(key, value);
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(final IndexedExpression indexedExpression) {
		indexedExpression.getIndexed().accept(this);
		final Object index = context.eval(indexedExpression.getIndex());
		if (index instanceof BigInteger) {
			final Map<BigInteger, Object> array = value == null ? new TreeMap<BigInteger, Object>()
					: (Map<BigInteger, Object>) value;
			if (!(value instanceof TreeMap)) {
				throw new InterpreterException("invalid index");
			}
			value = array;
			setter = (final Object value) -> {
				array.put((BigInteger) index, value);
			};
		} else if (index instanceof String) {
			final Map<String, Object> dictionary = value == null ? new HashMap<String, Object>()
					: (Map<String, Object>) value;
			if (!(value instanceof HashMap)) {
				throw new InterpreterException("invalid index");
			}
			value = dictionary;
			setter = (final Object value) -> {
				dictionary.put((String) index, value);
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
