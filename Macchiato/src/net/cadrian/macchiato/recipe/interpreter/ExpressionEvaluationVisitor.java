package net.cadrian.macchiato.recipe.interpreter;

import java.math.BigInteger;
import java.util.List;

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
import net.cadrian.macchiato.recipe.ast.expression.TypedBinary;
import net.cadrian.macchiato.recipe.ast.expression.TypedUnary;

public class ExpressionEvaluationVisitor implements ExpressionVisitor {

	private final Context context;
	private final Class<?> resultType;
	private Object result;

	ExpressionEvaluationVisitor(final Context context, final Class<?> resultType) {
		this.context = context;
		this.resultType = resultType;
	}

	public Object getResult() {
		return resultType.cast(result);
	}

	@Override
	public void visit(final TypedUnary typedUnary) {
		typedUnary.getOperand().accept(this);
		final Object operand = result;
		switch (typedUnary.getOperator()) {
		case NOT:
			if (!(operand instanceof Boolean)) {
				throw new InterpreterException("invalid operand type");
			}
			result = ((Boolean) operand) ? Boolean.FALSE : Boolean.TRUE;
			break;
		case MINUS:
			if (!(operand instanceof BigInteger)) {
				throw new InterpreterException("invalid operand type");
			}
			result = ((BigInteger) operand).negate();
			break;
		default:
			throw new InterpreterException("BUG: not implemented");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(final TypedBinary typedBinary) {
		typedBinary.getLeftOperand().accept(this);
		final Object left = result;
		switch (typedBinary.getOperator()) {
		case ADD:
			if (!(left instanceof String) && (left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			if (result instanceof String) {
				result = ((String) left) + (String) result;
			} else if (result instanceof BigInteger) {
				result = ((BigInteger) left).add((BigInteger) result);
			}
			break;
		case AND:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			if (Boolean.TRUE.equals(result)) {
				typedBinary.getRightOperand().accept(this);
				if (!(result instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type");
				}
			}
			break;
		case DIVIDE:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(result instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			result = ((BigInteger) left).divide((BigInteger) result);
			break;
		case EQ:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			result = Boolean.valueOf(((Comparable<Object>) left).compareTo((Comparable<Object>) result) == 0);
			break;
		case GE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			result = Boolean.valueOf(((Comparable<Object>) left).compareTo((Comparable<Object>) result) >= 0);
			break;
		case GT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			result = Boolean.valueOf(((Comparable<Object>) left).compareTo((Comparable<Object>) result) > 0);
			break;
		case LE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			result = Boolean.valueOf(((Comparable<Object>) left).compareTo((Comparable<Object>) result) <= 0);
			break;
		case LT:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			result = Boolean.valueOf(((Comparable<Object>) left).compareTo((Comparable<Object>) result) < 0);
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
			if (!(result instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			result = ((BigInteger) left).multiply((BigInteger) result);
			break;
		case NE:
			if (!(left instanceof Comparable)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (result.getClass() != left.getClass()) {
				throw new InterpreterException("incompatible types");
			}
			result = Boolean.valueOf(((Comparable<Object>) left).compareTo((Comparable<Object>) result) != 0);
			break;
		case OR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			if (Boolean.FALSE.equals(result)) {
				typedBinary.getRightOperand().accept(this);
				if (!(result instanceof Boolean)) {
					throw new InterpreterException("invalid right operand type");
				}
			}
			break;
		case POWER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(result instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			result = ((BigInteger) left).pow(((BigInteger) result).intValueExact());
			break;
		case REMAINDER:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(result instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			result = ((BigInteger) left).remainder((BigInteger) result);
			break;
		case SUBTRACT:
			if (!(left instanceof BigInteger)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(result instanceof BigInteger)) {
				throw new InterpreterException("invalid right operand type");
			}
			result = ((BigInteger) left).subtract((BigInteger) result);
			break;
		case XOR:
			if (!(left instanceof Boolean)) {
				throw new InterpreterException("invalid left operand type");
			}
			typedBinary.getRightOperand().accept(this);
			if (!(result instanceof Boolean)) {
				throw new InterpreterException("invalid right operand type");
			}
			result = Boolean.valueOf(((Boolean) left).booleanValue() != ((Boolean) result).booleanValue());
			break;
		default:
			throw new InterpreterException("BUG: not implemented");
		}
	}

	@Override
	public void visit(final ManifestString manifestString) {
		result = manifestString.getValue();
	}

	@Override
	public void visit(final ManifestRegex manifestRegex) {
		result = manifestRegex.getValue();
	}

	@Override
	public void visit(final ManifestNumeric manifestNumeric) {
		result = manifestNumeric.getValue();
	}

	@Override
	public void visit(final ManifestDictionary manifestDictionary) {
		final Dictionary dictionary = new Dictionary();
		for (final ManifestDictionary.Entry entry : manifestDictionary.getExpressions()) {
			entry.getKey().accept(this);
			final String key = (String) result;
			entry.getExpression().accept(this);
			dictionary.set(key, result);
		}
		result = dictionary;
	}

	@Override
	public void visit(final ManifestArray manifestArray) {
		final Array array = new Array();
		BigInteger index = BigInteger.ZERO;
		for (final Expression expression : manifestArray.getExpressions()) {
			expression.accept(this);
			array.set(index, result);
			index = index.add(BigInteger.ONE);
		}
		result = array;
	}

	@Override
	public void visit(final IndexedExpression indexedExpression) {
		indexedExpression.getIndexed().accept(this);
		final Object target = result;
		if (target == null) {
			throw new InterpreterException("invalid target");
		}
		indexedExpression.getIndex().accept(this);
		final Object index = result;
		if (index instanceof BigInteger) {
			if (!(target instanceof Array)) {
				throw new InterpreterException("invalid target type");
			}
			result = ((Array) target).get((BigInteger) index);
		} else if (index instanceof String) {
			if (!(target instanceof Dictionary)) {
				throw new InterpreterException("invalid target type");
			}
			result = ((Dictionary) target).get((String) index);
		} else {
			throw new InterpreterException("invalid index type");
		}
	}

	@Override
	public void visit(final Identifier identifier) {
		result = context.get(identifier.getName());
	}

	@Override
	public void visit(final FunctionCall functionCall) {
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
		result = callContext.get("result");
	}

	@Override
	public void visit(final CheckedExpression e) {
		e.getToCheck().accept(this);
		if (result != null && !e.getType().isAssignableFrom(result.getClass())) {
			throw new InterpreterException("bad result type: expected " + e.getType().getSimpleName() + " but got "
					+ result.getClass().getSimpleName());
		}
	}

}
