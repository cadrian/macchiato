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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ExpressionEvaluationVisitor implements ExpressionVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluationVisitor.class);

	private final Context context;
	private final Class<?> expressionType;
	private Object lastValue;

	ExpressionEvaluationVisitor(final Context context, final Class<?> expressionType) {
		this.context = context;
		this.expressionType = expressionType;
	}

	public Object getLastValue() {
		return expressionType.cast(lastValue);
	}

	@Override
	public void visitTypedUnary(final TypedUnary typedUnary) {
		LOGGER.debug("<-- {}", typedUnary);
		typedUnary.getOperand().accept(this);
		final Object operand = lastValue;
		if (operand == null) {
			throw new InterpreterException("invalid null expression", typedUnary.getOperand().position());
		}
		switch (typedUnary.getOperator()) {
		case NOT:
			if (!(operand instanceof Boolean)) {
				throw new InterpreterException("invalid operand type", typedUnary.getOperand().position());
			}
			lastValue = ((Boolean) operand) ? Boolean.FALSE : Boolean.TRUE;
			break;
		case MINUS:
			if (!(operand instanceof BigInteger)) {
				throw new InterpreterException("invalid operand type", typedUnary.getOperand().position());
			}
			lastValue = ((BigInteger) operand).negate();
			break;
		default:
			throw new InterpreterException("BUG: not implemented", typedUnary.getOperand().position());
		}
		LOGGER.debug("--> {}", lastValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visitTypedBinary(final TypedBinary typedBinary) {
		LOGGER.debug("<-- {}", typedBinary);
		typedBinary.getLeftOperand().accept(this);
		final Object left = lastValue;
		if (left == null) {
			throw new InterpreterException("invalid null left expression", typedBinary.getLeftOperand().position());
		}
		switch (typedBinary.getOperator()) {
		case ADD:
			if (!(left instanceof String) && !(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			if (lastValue instanceof String) {
				lastValue = ((String) left) + (String) lastValue;
			} else if (lastValue instanceof BigInteger) {
				lastValue = ((BigInteger) left).add((BigInteger) lastValue);
			} else {
				throw new InterpreterException("BUG: invalid type", typedBinary.getLeftOperand().position());
			}
			break;
		case AND:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			if (Boolean.TRUE.equals(lastValue)) {
				typedBinary.getRightOperand().accept(this);
				if (!(lastValue instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type",
							typedBinary.getLeftOperand().position());
				}
			}
			break;
		case DIVIDE:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((BigInteger) left).divide((BigInteger) lastValue);
			break;
		case EQ:
			typedBinary.getRightOperand().accept(this);
			lastValue = Boolean.valueOf(left.equals(lastValue));
			break;
		case GE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) >= 0);
			break;
		case GT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) > 0);
			break;
		case LE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) <= 0);
			break;
		case LT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) < 0);
			break;
		case MATCH:
			if (!(left instanceof String)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			break;
		case MULTIPLY:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((BigInteger) left).multiply((BigInteger) lastValue);
			break;
		case NE:
			typedBinary.getRightOperand().accept(this);
			lastValue = Boolean.valueOf(!left.equals(lastValue));
			break;
		case OR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			if (Boolean.FALSE.equals(lastValue)) {
				typedBinary.getRightOperand().accept(this);
				if (!(lastValue instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type",
							typedBinary.getLeftOperand().position());
				}
			}
			break;
		case POWER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((BigInteger) left).pow(((BigInteger) lastValue).intValueExact());
			break;
		case REMAINDER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((BigInteger) left).remainder((BigInteger) lastValue);
			break;
		case SUBTRACT:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((BigInteger) left).subtract((BigInteger) lastValue);
			break;
		case XOR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof Boolean)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = Boolean.valueOf(((Boolean) left).booleanValue() != ((Boolean) lastValue).booleanValue());
			break;
		default:
			throw new InterpreterException("BUG: not implemented", typedBinary.getLeftOperand().position());
		}
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestString(final ManifestString manifestString) {
		LOGGER.debug("<-- {}", manifestString);
		lastValue = manifestString.getValue();
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestRegex(final ManifestRegex manifestRegex) {
		LOGGER.debug("<-- {}", manifestRegex);
		lastValue = manifestRegex.getValue();
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestNumeric(final ManifestNumeric manifestNumeric) {
		LOGGER.debug("<-- {}", manifestNumeric);
		lastValue = manifestNumeric.getValue();
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestDictionary(final ManifestDictionary manifestDictionary) {
		LOGGER.debug("<-- {}", manifestDictionary);
		final Dictionary dictionary = new Dictionary();
		for (final ManifestDictionary.Entry entry : manifestDictionary.getExpressions()) {
			entry.getKey().accept(this);
			final String key = (String) lastValue;
			entry.getExpression().accept(this);
			dictionary.set(key, lastValue);
		}
		lastValue = dictionary;
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestArray(final ManifestArray manifestArray) {
		LOGGER.debug("<-- {}", manifestArray);
		final Array array = new Array();
		BigInteger index = BigInteger.ZERO;
		for (final Expression expression : manifestArray.getExpressions()) {
			expression.accept(this);
			array.set(index, lastValue);
			index = index.add(BigInteger.ONE);
		}
		lastValue = array;
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitIndexedExpression(final IndexedExpression indexedExpression) {
		LOGGER.debug("<-- {}", indexedExpression);
		indexedExpression.getIndexed().accept(this);
		final Object target = lastValue;
		if (target == null) {
			throw new InterpreterException("invalid target", indexedExpression.getIndexed().position());
		}
		indexedExpression.getIndex().accept(this);
		final Object index = lastValue;
		if (index instanceof BigInteger) {
			if (!(target instanceof Array)) {
				throw new InterpreterException("invalid target type", indexedExpression.getIndexed().position());
			}
			lastValue = ((Array) target).get((BigInteger) index);
		} else if (index instanceof String) {
			if (!(target instanceof Dictionary)) {
				throw new InterpreterException("invalid target type", indexedExpression.getIndexed().position());
			}
			lastValue = ((Dictionary) target).get((String) index);
		} else {
			throw new InterpreterException("invalid index type", indexedExpression.getIndexed().position());
		}
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitIdentifier(final Identifier identifier) {
		LOGGER.debug("<-- {}", identifier);
		lastValue = context.get(identifier.getName());
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitResult(final Result result) {
		LOGGER.debug("<-- {}", result);
		this.lastValue = context.get("result");
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitFunctionCall(final FunctionCall functionCall) {
		LOGGER.debug("<-- {}", functionCall);
		final Function fn = context.getFunction(functionCall.getName());
		final int position = functionCall.position();
		if (fn == null) {
			throw new InterpreterException("unknown function " + functionCall.getName(), position);
		}
		final LocalContext callContext = new LocalContext(context);
		final String[] argNames = fn.getArgNames();
		final Class<?>[] argTypes = fn.getArgTypes();
		final List<Expression> arguments = functionCall.getArguments();
		if (argNames.length != arguments.size()) {
			throw new InterpreterException("invalid parameters", position);
		}
		for (int i = 0; i < argNames.length; i++) {
			final Expression argument = arguments.get(i);
			final Object value = context.eval(argument.typed(argTypes[i]));
			callContext.set(argNames[i], value);
		}
		fn.run(callContext, position);
		lastValue = fn.getResultType().cast(callContext.get("result"));
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitCheckedExpression(final CheckedExpression e) {
		LOGGER.debug("<-- {}", e);
		e.getToCheck().accept(this);
		if (lastValue != null && !e.getType().isAssignableFrom(lastValue.getClass())) {
			throw new InterpreterException("bad result type: expected " + e.getType().getSimpleName() + " but got "
					+ lastValue.getClass().getSimpleName(), e.position());
		}
		LOGGER.debug("--> {}", lastValue);
	}

}
