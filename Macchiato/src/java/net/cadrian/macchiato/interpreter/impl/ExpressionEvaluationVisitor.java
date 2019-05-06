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
package net.cadrian.macchiato.interpreter.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Function;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacFunction;
import net.cadrian.macchiato.interpreter.objects.MacMethod;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.interpreter.objects.MacPattern;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.interpreter.objects.container.MacArray;
import net.cadrian.macchiato.interpreter.objects.container.MacDictionary;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.expression.CheckedExpression;
import net.cadrian.macchiato.ruleset.ast.expression.ExistsExpression;
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

public class ExpressionEvaluationVisitor implements ExpressionVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluationVisitor.class);

	private final Context context;
	private final Class<? extends MacObject> expressionType;
	private MacObject lastValue;

	ExpressionEvaluationVisitor(final Context context, final Class<? extends MacObject> expressionType) {
		this.context = context;
		this.expressionType = expressionType;
	}

	public MacObject getLastValue() {
		return expressionType.cast(lastValue);
	}

	@Override
	public void visitTypedUnary(final TypedUnary typedUnary) {
		LOGGER.debug("<-- {}", typedUnary);
		typedUnary.getOperand().accept(this);
		final MacObject operand = lastValue;
		if (operand == null) {
			throw new InterpreterException("expression does not exist", typedUnary.getOperand().position());
		}
		switch (typedUnary.getOperator()) {
		case NOT:
			if (!(operand instanceof MacBoolean)) {
				throw new InterpreterException("invalid operand type", typedUnary.getOperand().position());
			}
			lastValue = ((MacBoolean) operand).not();
			break;
		case MINUS:
			if (!(operand instanceof MacNumber)) {
				throw new InterpreterException("invalid operand type", typedUnary.getOperand().position());
			}
			lastValue = ((MacNumber) operand).negate();
			break;
		default:
			throw new InterpreterException("BUG: not implemented", typedUnary.getOperand().position());
		}
		LOGGER.debug("--> {} => {}", typedUnary, lastValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visitTypedBinary(final TypedBinary typedBinary) {
		LOGGER.debug("<-- {}", typedBinary);
		typedBinary.getLeftOperand().accept(this);
		final MacObject left = lastValue;
		if (left == null) {
			throw new InterpreterException("left expression does not exist", typedBinary.getLeftOperand().position());
		}
		switch (typedBinary.getOperator()) {
		case ADD:
			if (!(left instanceof MacString) && !(left instanceof MacNumber)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			if (lastValue instanceof MacString) {
				lastValue = ((MacString) left).concat((MacString) lastValue);
			} else if (lastValue instanceof MacNumber) {
				lastValue = ((MacNumber) left).add((MacNumber) lastValue);
			} else {
				throw new InterpreterException("BUG: invalid type", typedBinary.getLeftOperand().position());
			}
			break;
		case AND:
			if (!(left instanceof MacBoolean)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			if (MacBoolean.TRUE.equals(lastValue)) {
				typedBinary.getRightOperand().accept(this);
				if (lastValue == null) {
					throw new InterpreterException("right expression does not exist",
							typedBinary.getLeftOperand().position());
				}
				if (!(lastValue instanceof MacBoolean)) {
					throw new InterpreterException("invalid right operand type",
							typedBinary.getLeftOperand().position());
				}
			}
			break;
		case DIVIDE:
			if (!(left instanceof MacNumber)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacNumber)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacNumber) left).divide((MacNumber) lastValue);
			break;
		case EQ:
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			lastValue = MacBoolean.valueOf(left.equals(lastValue));
			break;
		case GE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = MacBoolean.valueOf(((Comparable<MacObject>) left).compareTo(lastValue) >= 0);
			break;
		case GT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = MacBoolean.valueOf(((Comparable<MacObject>) left).compareTo(lastValue) > 0);
			break;
		case LE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = MacBoolean.valueOf(((Comparable<MacObject>) left).compareTo(lastValue) <= 0);
			break;
		case LT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types", typedBinary.getLeftOperand().position());
			}
			lastValue = MacBoolean.valueOf(((Comparable<MacObject>) left).compareTo(lastValue) < 0);
			break;
		case MATCH:
			if (!(left instanceof MacString)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacPattern)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacPattern) lastValue).matches((MacString) left);
			break;
		case MULTIPLY:
			if (!(left instanceof MacNumber)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacNumber)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacNumber) left).multiply((MacNumber) lastValue);
			break;
		case NE:
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			lastValue = MacBoolean.valueOf(!left.equals(lastValue));
			break;
		case OR:
			if (!(left instanceof MacBoolean)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			if (MacBoolean.FALSE.equals(lastValue)) {
				typedBinary.getRightOperand().accept(this);
				if (lastValue == null) {
					throw new InterpreterException("right expression does not exist",
							typedBinary.getLeftOperand().position());
				}
				if (!(lastValue instanceof MacBoolean)) {
					throw new InterpreterException("invalid right operand type",
							typedBinary.getLeftOperand().position());
				}
			}
			break;
		case POWER:
			if (!(left instanceof MacNumber)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacNumber)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacNumber) left).pow((MacNumber) lastValue);
			break;
		case REMAINDER:
			if (!(left instanceof MacNumber)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacNumber)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacNumber) left).remainder((MacNumber) lastValue);
			break;
		case SUBTRACT:
			if (!(left instanceof MacNumber)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacNumber)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacNumber) left).subtract((MacNumber) lastValue);
			break;
		case XOR:
			if (!(left instanceof MacBoolean)) {
				throw new InterpreterException("invalid left operand type", typedBinary.getLeftOperand().position());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue == null) {
				throw new InterpreterException("right expression does not exist",
						typedBinary.getLeftOperand().position());
			}
			if (!(lastValue instanceof MacBoolean)) {
				throw new InterpreterException("invalid right operand type", typedBinary.getLeftOperand().position());
			}
			lastValue = ((MacBoolean) left).xor((MacBoolean) lastValue);
			break;
		default:
			throw new InterpreterException("BUG: not implemented", typedBinary.getLeftOperand().position());
		}
		LOGGER.debug("--> {} => {}", typedBinary, lastValue);
	}

	@Override
	public void visitManifestString(final ManifestString manifestString) {
		LOGGER.debug("<-- {}", manifestString);
		lastValue = MacString.valueOf(manifestString.getValue());
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestRegex(final ManifestRegex manifestRegex) {
		LOGGER.debug("<-- {}", manifestRegex);
		lastValue = MacPattern.valueOf(manifestRegex.getValue());
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestNumeric(final ManifestNumeric manifestNumeric) {
		LOGGER.debug("<-- {}", manifestNumeric);
		lastValue = MacNumber.valueOf(manifestNumeric.getValue());
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestDictionary(final ManifestDictionary manifestDictionary) {
		LOGGER.debug("<-- {}", manifestDictionary);
		final MacDictionary dictionary = new MacDictionary();
		for (final ManifestDictionary.Entry entry : manifestDictionary.getExpressions()) {
			entry.getKey().accept(this);
			final MacString key = (MacString) lastValue;
			entry.getExpression().accept(this);
			dictionary.set(key, lastValue);
		}
		lastValue = dictionary;
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestBoolean(final ManifestBoolean manifestBoolean) {
		LOGGER.debug("<-- {}", manifestBoolean);
		lastValue = MacBoolean.valueOf(manifestBoolean.getValue());
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitManifestArray(final ManifestArray manifestArray) {
		LOGGER.debug("<-- {}", manifestArray);
		final MacArray array = new MacArray();
		MacNumber index = MacNumber.ZERO;
		for (final Expression expression : manifestArray.getExpressions()) {
			expression.accept(this);
			array.set(index, lastValue);
			index = index.add(MacNumber.ONE);
		}
		lastValue = array;
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitIndexedExpression(final IndexedExpression indexedExpression) {
		LOGGER.debug("<-- {}", indexedExpression);
		indexedExpression.getIndexed().accept(this);
		final MacObject target = lastValue;
		if (target == null) {
			throw new InterpreterException("target does not exist", indexedExpression.getIndexed().position());
		}
		indexedExpression.getIndex().accept(this);
		final MacObject index = lastValue;
		if (index == null) {
			throw new InterpreterException("index does not exist", indexedExpression.getIndex().position());
		}
		if (index instanceof MacNumber) {
			if (!(target instanceof MacArray)) {
				throw new InterpreterException("invalid target type", indexedExpression.getIndexed().position());
			}
			lastValue = ((MacArray) target).get((MacNumber) index);
		} else if (index instanceof MacString) {
			final MacString name = (MacString) index;
			final MacObject value;
			if (target instanceof MacDictionary) {
				value = ((MacDictionary) target).get(name);
			} else {
				value = null;
			}
			if (value != null) {
				lastValue = value;
			} else {
				final MacMethod<? extends MacObject> method = getMethod(context, target, name.getValue());
				if (method != null) {
					lastValue = method;
				} else {
					throw new InterpreterException("invalid target type or unknown method " + name,
							indexedExpression.getIndexed().position());
				}
			}
		} else {
			throw new InterpreterException("invalid index type", indexedExpression.getIndexed().position());
		}
		LOGGER.debug("--> {} => {}", indexedExpression, lastValue);
	}

	private <T extends MacObject> MacMethod<? extends T> getMethod(final Context context, final T target,
			final String name) {
		final Method<T> method = target.getMethod(context.getRuleset(), name);
		if (method != null) {
			return new MacMethod<T>(method, target);
		}
		return null;
	}

	@Override
	public void visitIdentifier(final Identifier identifier) {
		LOGGER.debug("<-- {}", identifier);
		final String name = identifier.getName();
		final MacObject value = context.get(name);
		if (value != null) {
			lastValue = value;
		} else {
			final Function function = context.getFunction(name);
			if (function != null) {
				lastValue = new MacFunction(function);
			} else {
				lastValue = null;
			}
		}
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
		if (fn.getResultType() == null) {
			throw new InterpreterException("cannot assign this function because it does not return anything", position);
		}
		final LocalContext callContext = new LocalContext(context, fn.getRuleset());
		final String[] argNames = fn.getArgNames();
		final Class<? extends MacObject>[] argTypes = fn.getArgTypes();
		final List<Expression> arguments = functionCall.getArguments();
		if (argNames.length != arguments.size()) {
			throw new InterpreterException("invalid parameters (wrong arguments count)", position);
		}
		for (int i = 0; i < argNames.length; i++) {
			final Expression argument = arguments.get(i);
			final MacObject value = context.eval(argument.typed(argTypes[i]));
			callContext.declareLocal(argNames[i]);
			callContext.set(argNames[i], value);
		}
		callContext.declareLocal("result");
		fn.run(callContext, position);
		lastValue = fn.getResultType().cast(callContext.get("result"));
		LOGGER.debug("--> {} => {}", functionCall, lastValue);
	}

	@Override
	public void visitExistsExpression(final ExistsExpression existsExpression) {
		LOGGER.debug("<-- {}", existsExpression);
		existsExpression.getExpression().accept(this);
		lastValue = MacBoolean.valueOf(lastValue != null);
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visitCheckedExpression(final CheckedExpression checkedExpression) {
		LOGGER.debug("<-- {}", checkedExpression);
		checkedExpression.getToCheck().accept(this);
		if (lastValue != null && !checkedExpression.getType().isAssignableFrom(lastValue.getClass())) {
			throw new InterpreterException("bad result type: expected " + checkedExpression.getType().getSimpleName()
					+ " but got " + lastValue.getClass().getSimpleName(), checkedExpression.position());
		}
		LOGGER.debug("--> {}", lastValue);
	}

}
