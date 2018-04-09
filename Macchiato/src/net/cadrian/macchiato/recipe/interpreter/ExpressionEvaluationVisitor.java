package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.recipe.ast.Def;
import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.FormalArgs;
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
	private Object lastExpression;

	ExpressionEvaluationVisitor(final Context context, final Class<?> expressionType) {
		this.context = context;
		this.expressionType = expressionType;
	}

	public Object getLastExpression() {
		return expressionType.cast(lastExpression);
	}

	@Override
	public void visit(final TypedUnary typedUnary) {
		LOGGER.debug("<-- {}", typedUnary);
		typedUnary.getOperand().accept(this);
		final Object operand = lastExpression;
		if (operand == null) {
			throw new InterpreterException("invalid null expression");
		}
		switch (typedUnary.getOperator()) {
		case NOT:
			if (!(operand instanceof Boolean)) {
				throw new InterpreterException("invalid operand type");
			}
			lastExpression = ((Boolean) operand) ? Boolean.FALSE : Boolean.TRUE;
			break;
		case MINUS:
			if (!(operand instanceof BigInteger)) {
				throw new InterpreterException("invalid operand type");
			}
			lastExpression = ((BigInteger) operand).negate();
			break;
		default:
			throw new InterpreterException("BUG: not implemented");
		}
		LOGGER.debug("--> {}", lastExpression);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(final TypedBinary typedBinary) {
		LOGGER.debug("<-- {}", typedBinary);
		typedBinary.getLeftOperand().accept(this);
		final Object left = lastExpression;
		if (left == null) {
			throw new InterpreterException("invalid null left expression");
		}
		switch (typedBinary.getOperator()) {
		case ADD:
			if (!(left instanceof String) && (left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			if (lastExpression instanceof String) {
				lastExpression = ((String) left) + (String) lastExpression;
			} else if (lastExpression instanceof BigInteger) {
				lastExpression = ((BigInteger) left).add((BigInteger) lastExpression);
			}
			break;
		case AND:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			if (Boolean.TRUE.equals(lastExpression)) {
				typedBinary.getRightOperand().accept(this);
				if (!(lastExpression instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type");
				}
			}
			break;
		case DIVIDE:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastExpression instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastExpression = ((BigInteger) left).divide((BigInteger) lastExpression);
			break;
		case EQ:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type: " + left.getClass().getSimpleName());
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastExpression = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastExpression) == 0);
			break;
		case GE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastExpression = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastExpression) >= 0);
			break;
		case GT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastExpression = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastExpression) > 0);
			break;
		case LE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastExpression = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastExpression) <= 0);
			break;
		case LT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastExpression = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastExpression) < 0);
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
			if (!(lastExpression instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastExpression = ((BigInteger) left).multiply((BigInteger) lastExpression);
			break;
		case NE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (lastExpression.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			lastExpression = Boolean.valueOf(((Comparable<Object>) left).compareTo(lastExpression) != 0);
			break;
		case OR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			if (Boolean.FALSE.equals(lastExpression)) {
				typedBinary.getRightOperand().accept(this);
				if (!(lastExpression instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type");
				}
			}
			break;
		case POWER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastExpression instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastExpression = ((BigInteger) left).pow(((BigInteger) lastExpression).intValueExact());
			break;
		case REMAINDER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastExpression instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastExpression = ((BigInteger) left).remainder((BigInteger) lastExpression);
			break;
		case SUBTRACT:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastExpression instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastExpression = ((BigInteger) left).subtract((BigInteger) lastExpression);
			break;
		case XOR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(lastExpression instanceof Boolean)) {
				throw new InterpreterException("invalid right operand type");
			}
			lastExpression = Boolean
					.valueOf(((Boolean) left).booleanValue() != ((Boolean) lastExpression).booleanValue());
			break;
		default:
			throw new InterpreterException("BUG: not implemented");
		}
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final ManifestString manifestString) {
		LOGGER.debug("<-- {}", manifestString);
		lastExpression = manifestString.getValue();
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final ManifestRegex manifestRegex) {
		LOGGER.debug("<-- {}", manifestRegex);
		lastExpression = manifestRegex.getValue();
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final ManifestNumeric manifestNumeric) {
		LOGGER.debug("<-- {}", manifestNumeric);
		lastExpression = manifestNumeric.getValue();
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final ManifestDictionary manifestDictionary) {
		LOGGER.debug("<-- {}", manifestDictionary);
		final Dictionary dictionary = new Dictionary();
		for (final ManifestDictionary.Entry entry : manifestDictionary.getExpressions()) {
			entry.getKey().accept(this);
			final String key = (String) lastExpression;
			entry.getExpression().accept(this);
			dictionary.set(key, lastExpression);
		}
		lastExpression = dictionary;
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final ManifestArray manifestArray) {
		LOGGER.debug("<-- {}", manifestArray);
		final Array array = new Array();
		BigInteger index = BigInteger.ZERO;
		for (final Expression expression : manifestArray.getExpressions()) {
			expression.accept(this);
			array.set(index, lastExpression);
			index = index.add(BigInteger.ONE);
		}
		lastExpression = array;
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final IndexedExpression indexedExpression) {
		LOGGER.debug("<-- {}", indexedExpression);
		indexedExpression.getIndexed().accept(this);
		final Object target = lastExpression;
		if (target == null) {
			throw new InterpreterException("invalid target");
		}
		indexedExpression.getIndex().accept(this);
		final Object index = lastExpression;
		if (index instanceof BigInteger) {
			if (!(target instanceof Array)) {
				throw new InterpreterException("invalid target type");
			}
			lastExpression = ((Array) target).get((BigInteger) index);
		} else if (index instanceof String) {
			if (!(target instanceof Dictionary)) {
				throw new InterpreterException("invalid target type");
			}
			lastExpression = ((Dictionary) target).get((String) index);
		} else {
			throw new InterpreterException("invalid index type");
		}
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final Identifier identifier) {
		LOGGER.debug("<-- {}", identifier);
		lastExpression = context.get(identifier.getName());
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final Result result) {
		LOGGER.debug("<-- {}", result);
		this.lastExpression = context.get("result");
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final FunctionCall functionCall) {
		LOGGER.debug("<-- {}", functionCall);
		final Def def = context.getInterpreter().recipe.getDef(functionCall.getName());
		final LocalContext callContext = new LocalContext(context);
		final FormalArgs args = def.getArgs();
		final List<Expression> arguments = functionCall.getArguments();
		if (args.size() != arguments.size()) {
			throw new InterpreterException("invalid parameters");
		}
		for (int i = 0; i < args.size(); i++) {
			final Expression argument = arguments.get(i);
			final Object value = context.eval(argument.typed(Object.class));
			callContext.set(args.get(i), value);
		}
		def.getInstruction().accept(new InstructionEvaluationVisitor(callContext));
		lastExpression = callContext.get("result");
		LOGGER.debug("--> {}", lastExpression);
	}

	@Override
	public void visit(final CheckedExpression e) {
		LOGGER.debug("<-- {}", e);
		e.getToCheck().accept(this);
		if (lastExpression != null && !e.getType().isAssignableFrom(lastExpression.getClass())) {
			throw new InterpreterException("bad result type: expected " + e.getType().getSimpleName() + " but got "
					+ lastExpression.getClass().getSimpleName());
		}
		LOGGER.debug("--> {}", lastExpression);
	}

}
