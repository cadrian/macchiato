/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.expression.CheckedExpression;
import net.cadrian.macchiato.ruleset.ast.expression.ExpressionVisitor;
import net.cadrian.macchiato.ruleset.ast.expression.FunctionCall;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.ast.expression.IndexedExpression;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestArray;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestBoolean;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestDictionary;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestNumeric;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestRegex;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestString;
import net.cadrian.macchiato.ruleset.ast.expression.Result;
import net.cadrian.macchiato.ruleset.ast.expression.TypedBinary;
import net.cadrian.macchiato.ruleset.ast.expression.TypedUnary;

class AssignmentVisitor implements ExpressionVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentVisitor.class);

	private static interface Setter {
		void set(Object value);
	}

	private final Context context;

	private Object previousValue;
	private Setter setter;

	AssignmentVisitor(final Context context) {
		this.context = context;
	}

	void assign(final Expression target, final Object value) {
		previousValue = null;
		setter = null;
		target.accept(this);
		setter.set(value);
	}

	@Override
	public void visitCheckedExpression(final CheckedExpression e) {
		LOGGER.debug("<-- {}", e);
		e.getToCheck().accept(this);
		LOGGER.debug("-->");
	}

	@Override
	public void visitFunctionCall(final FunctionCall functionCall) {
		throw new InterpreterException("Cannot assign to a function call", functionCall.position());
	}

	private class IdentifierSetter implements Setter {
		private final Identifier identifier;

		IdentifierSetter(final Identifier identifier) {
			this.identifier = identifier;
		}

		@Override
		public void set(final Object value) {
			final String key = identifier.getName();
			LOGGER.debug("Setting global {} to {}", key, value);
			context.setGlobal(key, value);
		}
	}

	@Override
	public void visitIdentifier(final Identifier identifier) {
		LOGGER.debug("<-- {}", identifier);
		previousValue = context.get(identifier.getName());
		setter = new IdentifierSetter(identifier);
		LOGGER.debug("--> {}", previousValue);
	}

	private class ResultSetter implements Setter {

		ResultSetter() {
		}

		@Override
		public void set(final Object value) {
			LOGGER.debug("Setting result to {}", value);
			context.set("result", value);
		}
	}

	@Override
	public void visitResult(final Result result) {
		LOGGER.debug("<-- {}", result);
		previousValue = context.get("result");
		setter = new ResultSetter();
		LOGGER.debug("--> {}", previousValue);
	}

	private abstract class ContainerSetter<I, C extends Container<I>> implements Setter {
		private final C container;
		private final Setter setter;
		private final I index;

		ContainerSetter(final C container, final Setter setter, final I index) {
			this.container = container;
			this.setter = setter;
			this.index = index;
		}

		@Override
		public void set(final Object value) {
			final C actualContainer = container == null ? newContainer() : container;
			LOGGER.debug("--> setting container {} to {} at {}", actualContainer, value, index);
			actualContainer.set(index, value);
			setter.set(actualContainer);
			LOGGER.debug("<--");
		}

		protected abstract C newContainer();
	}

	private class ArraySetter extends ContainerSetter<BigInteger, Array> {
		ArraySetter(final Array container, final Setter setter, final BigInteger index) {
			super(container, setter, index);
		}

		@Override
		protected Array newContainer() {
			return new Array();
		}
	}

	private class DictionarySetter extends ContainerSetter<String, Dictionary> {
		DictionarySetter(final Dictionary container, final Setter setter, final String index) {
			super(container, setter, index);
		}

		@Override
		protected Dictionary newContainer() {
			return new Dictionary();
		}
	}

	@Override
	public void visitIndexedExpression(final IndexedExpression indexedExpression) {
		LOGGER.debug("<-- {}", indexedExpression);
		final Object index = context.eval(indexedExpression.getIndex());
		indexedExpression.getIndexed().accept(this);
		final Setter indexedSetter = setter;
		if (index instanceof BigInteger) {
			if (previousValue != null && !(previousValue instanceof Array)) {
				throw new InterpreterException("invalid index", indexedExpression.getIndex().position());
			}
			setter = new ArraySetter((Array) previousValue, indexedSetter, (BigInteger) index);
			previousValue = ((Array) previousValue).get((BigInteger) index);
		} else if (index instanceof String) {
			if (previousValue != null && !(previousValue instanceof Dictionary)) {
				throw new InterpreterException("invalid index", indexedExpression.getIndex().position());
			}
			setter = new DictionarySetter((Dictionary) previousValue, indexedSetter, (String) index);
			previousValue = ((Dictionary) previousValue).get((String) index);
		} else {
			throw new InterpreterException("Cannot use " + index.getClass().getSimpleName() + " as index",
					indexedExpression.position());
		}
		LOGGER.debug("--> {}", previousValue);
	}

	@Override
	public void visitManifestArray(final ManifestArray array) {
		throw new InterpreterException("Invalid left-side assignment", array.position());
	}

	@Override
	public void visitManifestBoolean(final ManifestBoolean manifestBoolean) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitManifestDictionary(final ManifestDictionary dictionary) {
		throw new InterpreterException("Invalid left-side assignment", dictionary.position());
	}

	@Override
	public void visitManifestNumeric(final ManifestNumeric manifestNumeric) {
		throw new InterpreterException("Invalid left-side assignment", manifestNumeric.position());
	}

	@Override
	public void visitManifestRegex(final ManifestRegex manifestRegex) {
		throw new InterpreterException("Invalid left-side assignment", manifestRegex.position());
	}

	@Override
	public void visitManifestString(final ManifestString manifestString) {
		throw new InterpreterException("Invalid left-side assignment", manifestString.position());
	}

	@Override
	public void visitTypedBinary(final TypedBinary typedBinary) {
		throw new InterpreterException("Invalid left-side assignment", typedBinary.position());
	}

	@Override
	public void visitTypedUnary(final TypedUnary typedUnary) {
		throw new InterpreterException("Invalid left-side assignment", typedUnary.position());
	}

}
