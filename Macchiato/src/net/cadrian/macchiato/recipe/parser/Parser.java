package net.cadrian.macchiato.recipe.parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.regex.Pattern;

import net.cadrian.macchiato.recipe.ast.BoundFilter;
import net.cadrian.macchiato.recipe.ast.ConditionFilter;
import net.cadrian.macchiato.recipe.ast.Def;
import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Filter;
import net.cadrian.macchiato.recipe.ast.FormalArgs;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Recipe;
import net.cadrian.macchiato.recipe.ast.RegexMatcher;
import net.cadrian.macchiato.recipe.ast.expression.Array;
import net.cadrian.macchiato.recipe.ast.expression.Binary;
import net.cadrian.macchiato.recipe.ast.expression.Dictionary;
import net.cadrian.macchiato.recipe.ast.expression.FunctionCall;
import net.cadrian.macchiato.recipe.ast.expression.Identifier;
import net.cadrian.macchiato.recipe.ast.expression.IndexedExpression;
import net.cadrian.macchiato.recipe.ast.expression.ManifestNumeric;
import net.cadrian.macchiato.recipe.ast.expression.ManifestRegex;
import net.cadrian.macchiato.recipe.ast.expression.ManifestString;
import net.cadrian.macchiato.recipe.ast.expression.TypedBinary;
import net.cadrian.macchiato.recipe.ast.expression.TypedExpression;
import net.cadrian.macchiato.recipe.ast.expression.TypedUnary;
import net.cadrian.macchiato.recipe.ast.expression.Unary;
import net.cadrian.macchiato.recipe.ast.instruction.Assignment;
import net.cadrian.macchiato.recipe.ast.instruction.Block;
import net.cadrian.macchiato.recipe.ast.instruction.Emit;
import net.cadrian.macchiato.recipe.ast.instruction.If;
import net.cadrian.macchiato.recipe.ast.instruction.Next;
import net.cadrian.macchiato.recipe.ast.instruction.ProcedureCall;
import net.cadrian.macchiato.recipe.ast.instruction.While;
import net.cadrian.macchiato.recipe.interpreter.AbstractEvent;

public class Parser {

	private final ParserBuffer buffer;

	public Parser(final Reader reader) throws IOException {
		this.buffer = new ParserBuffer(reader);
	}

	public Recipe parse() {
		final Recipe result = new Recipe();
		while (!buffer.off()) {
			if (readKeyword("def")) {
				skipBlanks();
				final int position = buffer.position();
				final Def old = result.addDef(parseDef());
				if (old != null) {
					throw new ParserException(buffer.error("Duplicate def " + old.name(), old.position(), position));
				}
			} else {
				result.addFilter(parseFilter());
			}
		}
		return result;
	}

	private Def parseDef() {
		skipBlanks();
		final int position = buffer.position();
		final String name = readIdentifier();
		if (name == null) {
			throw new ParserException(buffer.error("Expected def name"));
		}
		final FormalArgs args = parseFormalArgs();
		final Instruction inst = parseInstruction();
		final Def result = new Def(position, name, args, inst);
		return result;
	}

	private FormalArgs parseFormalArgs() {
		skipBlanks();
		if (buffer.current() != '(') {
			throw new ParserException(buffer.error("Expected formal arguments"));
		}
		final FormalArgs result = new FormalArgs();
		buffer.next();
		skipBlanks();
		if (buffer.current() == ')') {
			buffer.next();
		} else {
			boolean more = true;
			do {
				final String arg = readIdentifier();
				if (arg == null) {
					throw new ParserException(buffer.error("Expected arg name"));
				} else {
					result.add(arg);
					skipBlanks();
					switch (buffer.current()) {
					case ',':
						buffer.next();
						break;
					case ')':
						more = false;
						buffer.next();
						break;
					default:
						throw new ParserException(buffer.error("Invalid arguments list"));
					}
				}
			} while (more);
		}
		return null;
	}

	private Instruction parseInstruction() {
		skipBlanks();
		if (buffer.off()) {
			throw new ParserException(buffer.error("Expected instruction"));
		}
		if (buffer.current() == '{') {
			return parseBlock();
		}
		final int position = buffer.position();

		final String name = readRawIdentifier();
		if (name == null) {
			throw new ParserException(buffer.error("Expected instruction", position));
		}
		if (isReserved(name)) {
			switch (name) {
			case "if":
				return parseIf(position);
			case "switch":
				// TODO return parseSwitch();
				throw new ParserException(buffer.error("not yet implemented"));
			case "do":
				// TODO return parseDo();
				throw new ParserException(buffer.error("not yet implemented"));
			case "while":
				return parseWhile(position);
			case "emit":
				return parseEmit(position);
			case "next":
				return parseNext(position);
			default:
				throw new ParserException(buffer.error("Unexpected keyword " + name, position));
			}
		} else {
			skipBlanks();
			switch (buffer.current()) {
			case '[': {
				final Expression indexed = parseIndexed(new Identifier(position, name));
				skipBlanks();
				if (buffer.off() || buffer.current() != '=') {
					throw new ParserException(buffer.error("Expected assignment"));
				}
				buffer.next();
				final Expression exp = parseExpression();
				skipBlanks();
				if (buffer.current() == ';') {
					buffer.next();
				}
				return new Assignment(indexed, exp);
			}
			case '=':
				buffer.next();
				final Expression exp = parseExpression();
				skipBlanks();
				if (buffer.current() == ';') {
					buffer.next();
				}
				return new Assignment(new Identifier(position, name), exp);
			case '(':
				return parseProcedureCall(position, name);
			default:
				throw new ParserException(buffer.error("Expected assignment or function call"));
			}
		}
	}

	private Next parseNext(final int position) {
		skipBlanks();
		if (!buffer.off() && buffer.current() == ';') {
			buffer.next();
		}
		return new Next(position);
	}

	private Emit parseEmit(int position) {
		final Emit result;
		skipBlanks();
		if (buffer.off() || buffer.current() == ';') {
			result = new Emit(position, null);
		} else {
			final Expression expression = parseExpression();
			final TypedExpression<AbstractEvent> eventExpression = expression.typed(AbstractEvent.class);
			result = new Emit(position, eventExpression);
		}
		skipBlanks();
		if (!buffer.off() && buffer.current() == ';') {
			buffer.next();
		}
		return result;
	}

	private ProcedureCall parseProcedureCall(final int position, final String name) {
		final ProcedureCall result = new ProcedureCall(position, name);
		assert buffer.current() == '(';
		buffer.next();
		boolean more = true;
		do {
			final Expression exp = parseExpression();
			result.add(exp);
			skipBlanks();
			switch (buffer.current()) {
			case ',':
				buffer.next();
				break;
			case ')':
				buffer.next();
				more = false;
				break;
			default:
				throw new ParserException(buffer.error("Unexpected character"));
			}
		} while (more);
		skipBlanks();
		if (buffer.current() == ';') {
			buffer.next();
		}
		return result;
	}

	private If parseIf(final int position) {
		final Expression cond = parseExpression();
		skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(buffer.error("Expected block"));
		}
		final Block inst = parseBlock();
		final Block other;
		skipBlanks();
		if (readKeyword("else")) {
			other = parseBlock();
		} else {
			other = null;
		}
		return new If(position, cond, inst, other);
	}

	private While parseWhile(final int position) {
		final Expression cond = parseExpression();
		skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(buffer.error("Expected block"));
		}
		final Block inst = parseBlock();
		final Block other;
		skipBlanks();
		if (readKeyword("else")) {
			other = parseBlock();
		} else {
			other = null;
		}
		return new While(position, cond, inst, other);
	}

	private Block parseBlock() {
		assert buffer.current() == '{';
		final int position = buffer.position();
		final Block result = new Block(position);
		buffer.next();
		boolean more = true;
		do {
			skipBlanks();
			if (buffer.off()) {
				throw new ParserException(buffer.error("Unexpected end of text in block"));
			}
			if (buffer.current() == '}') {
				buffer.next();
				more = false;
			} else {
				final Instruction instruction = parseInstruction();
				result.add(instruction);
			}
		} while (more);
		return result;
	}

	private Expression parseExpression() {
		return parseOrRight(parseOrLeft());
	}

	private Expression parseOrLeft() {
		return parseAndRight(parseAndLeft());
	}

	private Expression parseOrRight(final Expression left) {
		if (!readKeyword("or")) {
			return left;
		}
		final TypedExpression<Boolean> leftOperand = left.typed(Boolean.class);
		if (leftOperand == null) {
			throw new ParserException(buffer.error("Expected boolean expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression<Boolean> rightOperand = right.typed(Boolean.class);
		if (rightOperand == null) {
			throw new ParserException(buffer.error("Expected boolean expression", right.position()));
		}
		return parseOrRight(new TypedBinary<Boolean, Boolean, Boolean>(leftOperand, Binary.Operator.OR, rightOperand,
				Boolean.class));
	}

	private Expression parseAndLeft() {
		return parseComparatorRight(parseComparatorLeft());
	}

	private Expression parseAndRight(final Expression left) {
		if (!readKeyword("and")) {
			return left;
		}
		final TypedExpression<Boolean> leftOperand = left.typed(Boolean.class);
		if (leftOperand == null) {
			throw new ParserException(buffer.error("Expected boolean expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression<Boolean> rightOperand = right.typed(Boolean.class);
		if (rightOperand == null) {
			throw new ParserException(buffer.error("Expected boolean expression", right.position()));
		}
		return parseAndRight(new TypedBinary<Boolean, Boolean, Boolean>(leftOperand, Binary.Operator.AND, rightOperand,
				Boolean.class));
	}

	private Expression parseComparatorLeft() {
		return parseAdditionRight(parseAdditionLeft());
	}

	private Expression parseComparatorRight(final Expression left) {
		final Binary.Operator comparator = readComparator();
		if (comparator == null) {
			return left;
		}
		final Expression result;
		final Expression right = parseExpression();
		if (comparator == Binary.Operator.MATCH) {
			final TypedExpression<String> leftOperand = left.typed(String.class);
			if (leftOperand == null) {
				throw new ParserException(buffer.error("Expected string", left.position()));
			}
			final TypedExpression<Pattern> rightOperand = right.typed(Pattern.class);
			if (rightOperand == null) {
				throw new ParserException(buffer.error("Expected regular expression", right.position()));
			}
			result = new RegexMatcher(leftOperand, rightOperand);
		} else {
			final TypedExpression<Comparable<?>> leftOperand = left.typed(Comparable.class);
			if (leftOperand == null) {
				throw new ParserException(buffer.error("Expected comparable expression", left.position()));
			}
			final TypedExpression<Comparable<?>> rightOperand = right.typed(Comparable.class);
			if (rightOperand == null) {
				throw new ParserException(buffer.error("Expected comparable expression", right.position()));
			}
			result = new TypedBinary<Comparable<?>, Comparable<?>, Boolean>(leftOperand, comparator, rightOperand,
					Boolean.class);
		}
		return parseComparatorRight(result);
	}

	private Binary.Operator readComparator() {
		skipBlanks();
		final int position = buffer.position();
		if (buffer.off()) {
			return null;
		}
		final char c1 = buffer.current();
		buffer.next();
		final char c2 = buffer.off() ? '\0' : buffer.current();
		switch (c1) {
		case '=':
			switch (c2) {
			case '=':
				buffer.next();
				return Binary.Operator.EQ;
			default:
				buffer.rewind(position);
				return null;
			}
		case '!':
			switch (c2) {
			case '=':
				buffer.next();
				return Binary.Operator.NE;
			default:
				buffer.rewind(position);
				return null;
			}
		case '~':
			return Binary.Operator.MATCH;
		case '<':
			switch (c2) {
			case '=':
				buffer.next();
				return Binary.Operator.LE;
			default:
				return Binary.Operator.LT;
			}
		case '>':
			switch (c2) {
			case '=':
				return Binary.Operator.GE;
			default:
				return Binary.Operator.GT;
			}
		default:
			buffer.rewind(position);
			return null;
		}
	}

	private Expression parseAdditionLeft() {
		return parseMultiplicationRight(parseMultiplicationLeft());
	}

	private Expression parseAdditionRight(final Expression left) {
		skipBlanks();
		if (buffer.off()) {
			return left;
		}
		switch (buffer.current()) {
		case '+': {
			buffer.next();
			final TypedExpression<Comparable<?>> leftOperand = left.typed(Comparable.class);
			if (leftOperand == null) {
				throw new ParserException(buffer.error("Expected comparable expression", left.position()));
			}
			final Expression right = parseExpression();
			final TypedExpression<Comparable<?>> rightOperand = right.typed(Comparable.class);
			if (rightOperand == null) {
				throw new ParserException(buffer.error("Expected comparable expression", right.position()));
			}
			@SuppressWarnings("unchecked")
			final Class<? extends Comparable<?>> resultType = (Class<? extends Comparable<?>>) Comparable.class;
			return parseAdditionRight(new TypedBinary<Comparable<?>, Comparable<?>, Comparable<?>>(leftOperand,
					Binary.Operator.ADD, rightOperand, resultType));
		}
		case '-': {
			buffer.next();
			final TypedExpression<BigInteger> leftOperand = left.typed(BigInteger.class);
			if (leftOperand == null) {
				throw new ParserException(buffer.error("Expected numeric expression", left.position()));
			}
			final Expression right = parseExpression();
			final TypedExpression<BigInteger> rightOperand = right.typed(BigInteger.class);
			if (rightOperand == null) {
				throw new ParserException(buffer.error("Expected numeric expression", right.position()));
			}
			return parseAdditionRight(new TypedBinary<BigInteger, BigInteger, BigInteger>(leftOperand,
					Binary.Operator.SUBTRACT, rightOperand, BigInteger.class));
		}
		default:
			return left;
		}
	}

	private Expression parseMultiplicationLeft() {
		return parsePowerRight(parsePowerLeft());
	}

	private Expression parseMultiplicationRight(final Expression left) {
		skipBlanks();
		if (buffer.off()) {
			return left;
		}
		final Binary.Operator operator;
		switch (buffer.current()) {
		case '*':
			operator = Binary.Operator.MULTIPLY;
			break;
		case '/':
			operator = Binary.Operator.DIVIDE;
			break;
		case '\\':
			operator = Binary.Operator.REMAINDER;
			break;
		default:
			return left;
		}
		buffer.next();
		final TypedExpression<BigInteger> leftOperand = left.typed(BigInteger.class);
		if (leftOperand == null) {
			throw new ParserException(buffer.error("Expected comparable expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression<BigInteger> rightOperand = right.typed(BigInteger.class);
		if (rightOperand == null) {
			throw new ParserException(buffer.error("Expected comparable expression", right.position()));
		}
		return parseMultiplicationRight(new TypedBinary<Comparable<?>, Comparable<?>, Comparable<?>>(leftOperand,
				operator, rightOperand, BigInteger.class));
	}

	private Expression parsePowerLeft() {
		return parseUnary();
	}

	private Expression parsePowerRight(final Expression left) {
		skipBlanks();
		if (buffer.off() || buffer.current() != '^') {
			return left;
		}
		final TypedExpression<BigInteger> leftOperand = left.typed(BigInteger.class);
		if (leftOperand == null) {
			throw new ParserException(buffer.error("Expected numeric expression", left.position()));
		}
		final Expression right = parsePowerRight(parsePowerLeft());
		final TypedExpression<BigInteger> rightOperand = right.typed(BigInteger.class);
		if (rightOperand == null) {
			throw new ParserException(buffer.error("Expected comparable expression", right.position()));
		}
		return new TypedBinary<Comparable<?>, Comparable<?>, Comparable<?>>(leftOperand, Binary.Operator.POWER,
				rightOperand, BigInteger.class);
	}

	private Expression parseUnary() {
		skipBlanks();
		if (buffer.off()) {
			throw new ParserException(buffer.error("Expected expression"));
		}
		final int position = buffer.position();
		if (readKeyword("not")) {
			final Expression operand = parseUnary();
			final TypedExpression<Boolean> typedOperand = operand.typed(Boolean.class);
			if (typedOperand == null) {
				throw new ParserException(buffer.error("Expected boolean expression", operand.position()));
			}
			return new TypedUnary<Boolean, Boolean>(position, Unary.Operator.NOT, typedOperand, Boolean.class);
		} else {
			switch (buffer.current()) {
			case '+': {
				buffer.next();
				final Expression operand = parseUnary();
				final TypedExpression<BigInteger> typedOperand = operand.typed(BigInteger.class);
				if (typedOperand == null) {
					throw new ParserException(buffer.error("Expected comparable expression", operand.position()));
				}
				return operand;
			}
			case '-':
				buffer.next(); {
				final Expression operand = parseUnary();
				final TypedExpression<BigInteger> typedOperand = operand.typed(BigInteger.class);
				if (typedOperand == null) {
					throw new ParserException(buffer.error("Expected comparable expression", operand.position()));
				}
				return new TypedUnary<Comparable<?>, Comparable<?>>(position, Unary.Operator.MINUS, typedOperand,
						BigInteger.class);
			}
			}
		}
		return parseAtomicExpression();
	}

	private Expression parseAtomicExpression() {
		final Expression result;
		if (buffer.off()) {
			throw new ParserException(buffer.error("Expected expression"));
		}
		final int position = buffer.position();
		if (buffer.current() == '(') {
			buffer.next();
			result = parseExpression();
			skipBlanks();
			if (buffer.off() || buffer.current() != ')') {
				throw new ParserException(buffer.error("Unfinished expresssion"));
			}
			buffer.next();
		} else {
			switch (buffer.current()) {
			case '"':
				result = parseString();
				break;
			case '/':
				result = parseRegex();
				break;
			case '[':
				result = parseArray();
				break;
			case '{':
				result = parseDictionary();
				break;
			default:
				if (Character.isDigit(buffer.current())) {
					result = parseNumber();
				} else {
					final String name = readIdentifier();
					skipBlanks();
					if (buffer.off() || buffer.current() != '(') {
						result = new Identifier(position, name);
					} else {
						result = parseFunctionCall(position, name);
					}
				}
			}
		}
		return parseIndexed(result);
	}

	private Expression parseIndexed(Expression expression) {
		skipBlanks();
		while (!buffer.off() && buffer.current() == '[') {
			buffer.next();
			final Expression index = parseExpression();
			skipBlanks();
			if (buffer.off() || buffer.current() != ']') {
				throw new ParserException(buffer.error("Missing closing bracket"));
			}
			buffer.next();
			final TypedExpression<Comparable<?>> typedIndex = index.typed(Comparable.class);
			if (typedIndex == null) {
				throw new ParserException(buffer.error("Expected numeric or string index", index.position()));
			}
			expression = new IndexedExpression(expression, typedIndex);
		}
		return expression;
	}

	private TypedExpression<String> parseString() {
		assert buffer.current() == '"';
		final int position = buffer.position();
		buffer.next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (buffer.off()) {
				throw new ParserException(buffer.error("Unfinished string", position));
			}
			switch (state) {
			case 0: // normal
				switch (buffer.current()) {
				case '\\':
					state = 1;
					break;
				case '"':
					state = -1;
					break;
				default:
					b.append(buffer.current());
				}
				break;
			case 1: // backslash
				switch (buffer.current()) {
				case 't':
					b.append('\t');
					break;
				case 'n':
					b.append('\n');
					break;
				default:
					b.append(buffer.current());
				}
				break;
			default:
				throw new ParserException(buffer.error("BUG: unexpected state " + state, position));
			}
			buffer.next();
		} while (state >= 0);
		return new ManifestString(position, b.toString());
	}

	private TypedExpression<Pattern> parseRegex() {
		assert buffer.current() == '/';
		final int position = buffer.position();
		buffer.next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (buffer.off()) {
				throw new ParserException(buffer.error("Unfinished string", position));
			}
			switch (state) {
			case 0: // normal
				switch (buffer.current()) {
				case '\\':
					state = 1;
					break;
				case '/':
					state = -1;
					break;
				default:
					b.append(buffer.current());
				}
				break;
			case 1: // backslash
				switch (buffer.current()) {
				case 't':
					b.append('\t');
					break;
				case 'n':
					b.append('\n');
					break;
				default:
					b.append(buffer.current());
				}
				break;
			default:
				throw new ParserException(buffer.error("BUG: unexpected state " + state, position));
			}
			buffer.next();
		} while (state >= 0);
		return new ManifestRegex(position, Pattern.compile(b.toString()));
	}

	private Array parseArray() {
		assert buffer.current() == '[';
		final Array result = new Array(buffer.position());
		buffer.next();
		boolean more = true;
		do {
			final Expression exp = parseExpression();
			result.add(exp);
			skipBlanks();
			switch (buffer.current()) {
			case ',':
				buffer.next();
				break;
			case ']':
				buffer.next();
				more = false;
				break;
			default:
				throw new ParserException(buffer.error("Unexpected character"));
			}
		} while (more);
		return result;
	}

	private Dictionary parseDictionary() {
		assert buffer.current() == '{';
		final Dictionary result = new Dictionary(buffer.position());
		buffer.next();
		boolean more = true;
		do {
			final Expression key = parseExpression();
			skipBlanks();
			if (buffer.off() || buffer.current() != ':') {
				throw new ParserException(buffer.error("Invalid dictionary", buffer.position()));
			}
			final TypedExpression<Comparable<?>> typedKey = key.typed(Comparable.class);
			if (typedKey == null) {
				throw new ParserException(buffer.error("Invalid dictionary key", key.position()));
			}
			final Expression exp = parseExpression();
			result.put(typedKey, exp);
			skipBlanks();
			switch (buffer.current()) {
			case ',':
				buffer.next();
				break;
			case '}':
				buffer.next();
				more = false;
				break;
			default:
				throw new ParserException(buffer.error("Unexpected character"));
			}
		} while (more);
		return result;
	}

	private TypedExpression<BigInteger> parseNumber() {
		assert Character.isDigit(buffer.current());
		final int position = buffer.position();
		final StringBuilder b = new StringBuilder();
		while (!buffer.off() && Character.isDigit(buffer.current())) {
			b.append(buffer.current());
			buffer.next();
		}
		final BigInteger value = new BigInteger(b.toString());
		return new ManifestNumeric(position, value);
	}

	private FunctionCall parseFunctionCall(final int position, final String name) {
		final FunctionCall result = new FunctionCall(position, name);
		assert buffer.current() == '(';
		buffer.next();
		boolean more = true;
		do {
			final Expression exp = parseExpression();
			result.add(exp);
			skipBlanks();
			switch (buffer.current()) {
			case ',':
				buffer.next();
				break;
			case ')':
				buffer.next();
				more = false;
				break;
			default:
				throw new ParserException(buffer.error("Unexpected character"));
			}
		} while (more);
		skipBlanks();
		if (buffer.current() == ';') {
			buffer.next();
		}
		return result;
	}

	private Filter parseFilter() {
		skipBlanks();
		final int position = buffer.position();
		final BoundFilter.Bound bound;
		if (readKeyword("BEGIN")) {
			if (readKeyword("SEQUENCE")) {
				bound = BoundFilter.Bound.BEGIN_SEQUENCE;
			} else if (readKeyword("TRACK")) {
				bound = BoundFilter.Bound.BEGIN_TRACK;
			} else {
				bound = null;
			}
		} else if (readKeyword("END")) {
			if (readKeyword("SEQUENCE")) {
				bound = BoundFilter.Bound.END_SEQUENCE;
			} else if (readKeyword("TRACK")) {
				bound = BoundFilter.Bound.END_TRACK;
			} else {
				bound = null;
			}
		} else {
			bound = null;
		}
		if (bound != null) {
			skipBlanks();
			if (buffer.off() || buffer.current() != '{') {
				throw new ParserException(buffer.error("Expected block"));
			}
			final Block instr = parseBlock();
			return new BoundFilter(position, bound, instr);
		} else {
			buffer.rewind(position);
			final Expression expr = parseExpression();
			final TypedExpression<Boolean> condition = expr.typed(Boolean.class);
			if (condition == null) {
				throw new ParserException(buffer.error("Expected boolean condition", expr.position()));
			}
			skipBlanks();
			if (buffer.off() || buffer.current() != '{') {
				throw new ParserException(buffer.error("Expected block"));
			}
			final Block instr = parseBlock();
			return new ConditionFilter(condition, instr);
		}
	}

	private String readIdentifier() {
		skipBlanks();
		if (buffer.off()) {
			return null;
		}
		final int position = buffer.position();
		final String result = readRawIdentifier();
		if (result == null || isReserved(result)) {
			buffer.rewind(position);
			return null;
		}
		return result;
	}

	private String readRawIdentifier() {
		final char first = buffer.current();
		if (!Character.isJavaIdentifierStart(first)) {
			return null;
		}
		buffer.next();
		final StringBuffer result = new StringBuffer().append(first);
		boolean more = true;
		do {
			if (buffer.off()) {
				more = false;
			} else {
				final char c = buffer.current();
				if (Character.isJavaIdentifierPart(c) || c == '.') {
					result.append(c);
					buffer.next();
				} else {
					more = false;
				}
			}
		} while (more);
		return result.toString();
	}

	private boolean isReserved(final String identifier) {
		switch (identifier) {
		case "def":
		case "if":
		case "else":
		case "switch":
		case "case":
		case "default":
		case "while":
		case "do":
		case "and":
		case "or":
		case "not":
		case "xor":
		case "emit":
		case "next":
			return true;
		default:
			return false;
		}
	}

	private boolean readKeyword(final String keyword) {
		skipBlanks();
		final int position = buffer.position();
		for (final char kw : keyword.toCharArray()) {
			if (buffer.off() || buffer.current() != kw) {
				buffer.rewind(position);
				return false;
			}
			buffer.next();
		}
		return true;
	}

	private void skipBlanks() {
		int state = 0;
		while (!buffer.off()) {
			switch (state) {
			case 0: // normal text
				switch (buffer.current()) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					buffer.next();
					break;
				case '#':
					state = 1;
					buffer.next();
					break;
				default:
					return;
				}
				break;
			case 1: // in comment
				switch (buffer.current()) {
				case '\r':
				case '\n':
					state = 0;
					break;
				}
				buffer.next();
			}
		}
	}

	public static void main(final String[] args) throws IOException {
		final FileReader fileReader = new FileReader(args[0]);
		final Parser parser = new Parser(fileReader);
		final Recipe recipe = parser.parse();
		System.out.println("Recipe: " + recipe);
	}

}
