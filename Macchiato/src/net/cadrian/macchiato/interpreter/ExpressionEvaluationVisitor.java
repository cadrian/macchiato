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
	public void visit(final TypedUnary typedUnary) {
		LOGGER.debug("<-- {}", typedUnary);
		typedUnary.getOperand().accept(this);
		final Object operand = lastValue;
		if (operand == null) {
			throw new InterpreterException("invalid null expression");
		}
		switch (typedUnary.getOperator()) {
		case NOT:
			if (!(operand instanceof Boolean)) {
				throw new InterpreterException("invalid operand type");
			}
			lastValue = ((Boolean) operand) ? Boolean.FALSE : Boolean.TRUE;
			break;
		case MINUS:
			if (!(operand instanceof BigInteger)) {
				throw new InterpreterException("invalid operand type");
			}
			lastValue = ((BigInteger) operand).negate();
			break;
		default:
			throw new InterpreterException("BUG: not implemented");
		}
		LOGGER.debug("--> {}", lastValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(final TypedBinary typedBinary) {
		LOGGER.debug("<-- {}", typedBinary);
		typedBinary.getLeftOperand().accept(this);
		final Object left = lastValue;
		if (left == null) {
			throw new InterpreterException("invalid null left expression");
		}
		switch (typedBinary.getOperator()) {
		case ADD:
			if (!(left instanceof String) && (left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			if (lastValue instanceof String) {
				lastValue = ((String) left) + (String) lastValue;
			} else if (lastValue instanceof BigInteger) {
				lastValue = ((BigInteger) left).add((BigInteger) lastValue);
			}
			break;
		case AND:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			if (Boolean.TRUE.equals(lastValue)) {
				typedBinary.getRightOperand().accept(this);
				if (!(lastValue instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type");
				}
			}
			break;
		case DIVIDE:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastValue = ((BigInteger) left).divide((BigInteger) lastValue);
			break;
		case EQ:
			typedBinary.getRightOperand().accept(this);
			lastValue = Boolean.valueOf(left.equals(lastValue));
			break;
		case GE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) >= 0);
			break;
		case GT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) > 0);
			break;
		case LE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) <= 0);
			break;
		case LT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastValue.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastValue = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastValue) < 0);
			break;
		case MATCH:
			if (!(left instanceof String)) {
				throw new InterpreterException("invalid left operand type");
			}
			break;
		case MULTIPLY:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastValue = ((BigInteger) left).multiply((BigInteger) lastValue);
			break;
		case NE:
			typedBinary.getRightOperand().accept(this);
			lastValue = Boolean.valueOf(!left.equals(lastValue));
			break;
		case OR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			if (Boolean.FALSE.equals(lastValue)) {
				typedBinary.getRightOperand().accept(this);
				if (!(lastValue instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type");
				}
			}
			break;
		case POWER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastValue = ((BigInteger) left).pow(((BigInteger) lastValue).intValueExact());
			break;
		case REMAINDER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastValue = ((BigInteger) left).remainder((BigInteger) lastValue);
			break;
		case SUBTRACT:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastValue = ((BigInteger) left).subtract((BigInteger) lastValue);
			break;
		case XOR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastValue instanceof Boolean)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastValue = Boolean.valueOf(((Boolean) left).booleanValue() != ((Boolean) lastValue).booleanValue());
			break;
		default:
			throw new InterpreterException("BUG: not implemented");
		}
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final ManifestString manifestString) {
		LOGGER.debug("<-- {}", manifestString);
		lastValue = manifestString.getValue();
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final ManifestRegex manifestRegex) {
		LOGGER.debug("<-- {}", manifestRegex);
		lastValue = manifestRegex.getValue();
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final ManifestNumeric manifestNumeric) {
		LOGGER.debug("<-- {}", manifestNumeric);
		lastValue = manifestNumeric.getValue();
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final ManifestDictionary manifestDictionary) {
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
	public void visit(final ManifestArray manifestArray) {
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
	public void visit(final IndexedExpression indexedExpression) {
		LOGGER.debug("<-- {}", indexedExpression);
		indexedExpression.getIndexed().accept(this);
		final Object target = lastValue;
		if (target == null) {
			throw new InterpreterException("invalid target");
		}
		indexedExpression.getIndex().accept(this);
		final Object index = lastValue;
		if (index instanceof BigInteger) {
			if (!(target instanceof Array)) {
				throw new InterpreterException("invalid target type");
			}
			lastValue = ((Array) target).get((BigInteger) index);
		} else if (index instanceof String) {
			if (!(target instanceof Dictionary)) {
				throw new InterpreterException("invalid target type");
			}
			lastValue = ((Dictionary) target).get((String) index);
		} else {
			throw new InterpreterException("invalid index type");
		}
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final Identifier identifier) {
		LOGGER.debug("<-- {}", identifier);
		lastValue = context.get(identifier.getName());
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final Result result) {
		LOGGER.debug("<-- {}", result);
		this.lastValue = context.get("result");
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final FunctionCall functionCall) {
		LOGGER.debug("<-- {}", functionCall);
		final Function fn = context.getFunction(functionCall.getName());
		if (fn == null) {
			throw new InterpreterException("unknown function " + functionCall.getName());
		}
		final LocalContext callContext = new LocalContext(context);
		final String[] argNames = fn.getArgNames();
		final Class<?>[] argTypes = fn.getArgTypes();
		final List<Expression> arguments = functionCall.getArguments();
		if (argNames.length != arguments.size()) {
			throw new InterpreterException("invalid parameters");
		}
		for (int i = 0; i < argNames.length; i++) {
			final Expression argument = arguments.get(i);
			final Object value = context.eval(argument.typed(argTypes[i]));
			callContext.set(argNames[i], value);
		}
		fn.run(callContext);
		lastValue = fn.getResultType().cast(callContext.get("result"));
		LOGGER.debug("--> {}", lastValue);
	}

	@Override
	public void visit(final CheckedExpression e) {
		LOGGER.debug("<-- {}", e);
		e.getToCheck().accept(this);
		if (lastValue != null && !e.getType().isAssignableFrom(lastValue.getClass())) {
			throw new InterpreterException("bad result type: expected " + e.getType().getSimpleName() + " but got "
					+ lastValue.getClass().getSimpleName());
		}
		LOGGER.debug("--> {}", lastValue);
	}

}
