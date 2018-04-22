package net.cadrian.macchiato.recipe.parser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.recipe.ast.BoundFilter;
import net.cadrian.macchiato.recipe.ast.ConditionFilter;
import net.cadrian.macchiato.recipe.ast.Def;
import net.cadrian.macchiato.recipe.ast.Expression;
import net.cadrian.macchiato.recipe.ast.Filter;
import net.cadrian.macchiato.recipe.ast.FormalArgs;
import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Recipe;
import net.cadrian.macchiato.recipe.ast.RegexMatcher;
import net.cadrian.macchiato.recipe.ast.expression.Binary;
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

public class Parser {

	private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

	private final ParserBuffer buffer;

	public Parser(final Reader reader) throws IOException {
		this.buffer = new ParserBuffer(reader);
	}

	public Recipe parse() {
		LOGGER.debug("<-- {}", buffer.position());
		final Recipe result = new Recipe();
		try {
			while (!buffer.off()) {
				if (readKeyword("def")) {
					skipBlanks();
					final int position = buffer.position();
					final Def old = result.addDef(parseDef());
					if (old != null) {
						throw new ParserException(error("Duplicate def " + old.name(), old.position(), position));
					}
				} else {
					result.addFilter(parseFilter());
				}
				skipBlanks();
			}
		} catch (final Exception e) {
			throw new ParserException(error(e.getMessage()), e);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Def parseDef() {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final int position = buffer.position();
		final String name = readIdentifier();
		if (name == null) {
			throw new ParserException(error("Expected def name"));
		}
		final FormalArgs args = parseFormalArgs();
		final Instruction inst = parseInstruction();
		final Def result = new Def(position, name, args, inst);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private FormalArgs parseFormalArgs() {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		if (buffer.current() != '(') {
			throw new ParserException(error("Expected formal arguments"));
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
					throw new ParserException(error("Expected arg name"));
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
						throw new ParserException(error("Invalid arguments list"));
					}
				}
			} while (more);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Instruction parseInstruction() {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		if (buffer.off()) {
			throw new ParserException(error("Expected instruction"));
		}
		if (buffer.current() == '{') {
			final Block result = parseBlock();
			LOGGER.debug("--> {}", result);
			return result;
		}
		final int position = buffer.position();

		final String name = readRawIdentifier();
		if (name == null) {
			throw new ParserException(error("Expected instruction", position));
		}
		if (isReserved(name)) {
			switch (name) {
			case "if": {
				final If result = parseIf(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "switch":
				// TODO return parseSwitch();
				throw new ParserException(error("not yet implemented"));
			case "do":
				// TODO return parseDo();
				throw new ParserException(error("not yet implemented"));
			case "while": {
				final While result = parseWhile(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "emit": {
				final Emit result = parseEmit(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "next": {
				final Next result = parseNext(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			default:
				throw new ParserException(error("Unexpected keyword " + name, position));
			}
		} else {
			skipBlanks();
			switch (buffer.current()) {
			case '[': {
				final Expression indexed = parseIdentifierSuffix(new Identifier(position, name));
				skipBlanks();
				if (buffer.off() || buffer.current() != '=') {
					throw new ParserException(error("Expected assignment"));
				}
				buffer.next();
				final Expression exp = parseExpression();
				skipBlanks();
				if (buffer.current() == ';') {
					buffer.next();
				}
				final Assignment result = new Assignment(indexed, exp);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case '=': {
				buffer.next();
				final Expression exp = parseExpression();
				skipBlanks();
				if (buffer.current() == ';') {
					buffer.next();
				}
				final Assignment result = new Assignment(new Identifier(position, name), exp);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case '(': {
				final ProcedureCall result = parseProcedureCall(position, name);
				LOGGER.debug("--> {}", result);
				return result;
			}
			default:
				throw new ParserException(error("Expected assignment or function call"));
			}
		}
	}

	private Next parseNext(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		if (!buffer.off() && buffer.current() == ';') {
			buffer.next();
		}
		final Next result = new Next(position);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Emit parseEmit(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		final Emit result;
		skipBlanks();
		if (buffer.off() || buffer.current() == ';') {
			result = new Emit(position, null, null);
		} else {
			final Expression expression = parseExpression();
			final TypedExpression messageExpression = expression.typed(Message.class);
			final TypedExpression tickExpression;
			skipBlanks();
			if (readKeyword("at")) {
				final Expression te = parseExpression();
				tickExpression = te.typed(BigInteger.class);
			} else {
				tickExpression = null;
			}
			result = new Emit(position, messageExpression, tickExpression);
		}
		skipBlanks();
		if (!buffer.off() && buffer.current() == ';') {
			buffer.next();
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private ProcedureCall parseProcedureCall(final int position, final String name) {
		LOGGER.debug("<-- {}", buffer.position());
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
				throw new ParserException(error("Unexpected character"));
			}
		} while (more);
		skipBlanks();
		if (buffer.current() == ';') {
			buffer.next();
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private If parseIf(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression cond = parseExpression();
		skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(error("Expected block"));
		}
		final Block inst = parseBlock();
		final Block other;
		skipBlanks();
		if (readKeyword("else")) {
			other = parseBlock();
		} else {
			other = null;
		}
		final If result = new If(position, cond, inst, other);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private While parseWhile(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression cond = parseExpression();
		skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(error("Expected block"));
		}
		final Block inst = parseBlock();
		final Block other;
		skipBlanks();
		if (readKeyword("else")) {
			other = parseBlock();
		} else {
			other = null;
		}
		final While result = new While(position, cond, inst, other);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Block parseBlock() {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '{';
		final int position = buffer.position();
		final Block result = new Block(position);
		buffer.next();
		boolean more = true;
		do {
			skipBlanks();
			if (buffer.off()) {
				throw new ParserException(error("Unexpected end of text in block"));
			}
			if (buffer.current() == '}') {
				buffer.next();
				more = false;
			} else {
				final Instruction instruction = parseInstruction();
				result.add(instruction);
			}
		} while (more);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseExpression() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseOrRight(parseOrLeft());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseOrLeft() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseAndRight(parseAndLeft());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseOrRight(final Expression left) {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result;
		if (!readKeyword("or")) {
			result = left;
		} else {
			final TypedExpression leftOperand = left.typed(Boolean.class);
			if (leftOperand == null) {
				throw new ParserException(error("Expected boolean expression", left.position()));
			}
			final Expression right = parseExpression();
			final TypedExpression rightOperand = right.typed(Boolean.class);
			if (rightOperand == null) {
				throw new ParserException(error("Expected boolean expression", right.position()));
			}
			result = parseOrRight(new TypedBinary(leftOperand, Binary.Operator.OR, rightOperand, Boolean.class));
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAndLeft() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseComparatorRight(parseComparatorLeft());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAndRight(final Expression left) {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result;
		if (!readKeyword("and")) {
			result = left;
		} else {
			final TypedExpression leftOperand = left.typed(Boolean.class);
			if (leftOperand == null) {
				throw new ParserException(error("Expected boolean expression", left.position()));
			}
			final Expression right = parseExpression();
			final TypedExpression rightOperand = right.typed(Boolean.class);
			if (rightOperand == null) {
				throw new ParserException(error("Expected boolean expression", right.position()));
			}
			result = parseAndRight(new TypedBinary(leftOperand, Binary.Operator.AND, rightOperand, Boolean.class));
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseComparatorLeft() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseAdditionRight(parseAdditionLeft());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseComparatorRight(final Expression left) {
		LOGGER.debug("<-- {}", buffer.position());
		final Binary.Operator comparator = readComparator();
		final Expression result;
		if (comparator == null) {
			result = left;
		} else {
			final Expression right = parseExpression();
			if (comparator == Binary.Operator.MATCH) {
				final TypedExpression leftOperand = left.typed(String.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected string", left.position()));
				}
				final TypedExpression rightOperand = right.typed(Pattern.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected regular expression", right.position()));
				}
				result = parseComparatorRight(new RegexMatcher(leftOperand, rightOperand));
			} else {
				final TypedExpression leftOperand = left.typed(Comparable.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected comparable expression", left.position()));
				}
				final TypedExpression rightOperand = right.typed(Comparable.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected comparable expression", right.position()));
				}
				result = parseComparatorRight(new TypedBinary(leftOperand, comparator, rightOperand, Boolean.class));
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
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
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseMultiplicationRight(parseMultiplicationLeft());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAdditionRight(final Expression left) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final Expression result;
		if (buffer.off()) {
			result = left;
		} else {
			switch (buffer.current()) {
			case '+': {
				buffer.next();
				final TypedExpression leftOperand = left.typed(Comparable.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected comparable expression", left.position()));
				}
				final Expression right = parseExpression();
				final TypedExpression rightOperand = right.typed(Comparable.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected comparable expression", right.position()));
				}
				@SuppressWarnings("unchecked")
				final Class<? extends Comparable<?>> resultType = (Class<? extends Comparable<?>>) Comparable.class;
				result = parseAdditionRight(
						new TypedBinary(leftOperand, Binary.Operator.ADD, rightOperand, resultType));
				break;
			}
			case '-': {
				buffer.next();
				final TypedExpression leftOperand = left.typed(BigInteger.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected numeric expression", left.position()));
				}
				final Expression right = parseExpression();
				final TypedExpression rightOperand = right.typed(BigInteger.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected numeric expression", right.position()));
				}
				result = parseAdditionRight(
						new TypedBinary(leftOperand, Binary.Operator.SUBTRACT, rightOperand, BigInteger.class));
				break;
			}
			default:
				result = left;
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseMultiplicationLeft() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parsePowerRight(parsePowerLeft());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseMultiplicationRight(final Expression left) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final Expression result;
		if (buffer.off()) {
			result = left;
		} else {
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
				operator = null;
			}
			if (operator == null) {
				result = left;
			} else {
				buffer.next();
				final TypedExpression leftOperand = left.typed(BigInteger.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected comparable expression", left.position()));
				}
				final Expression right = parseExpression();
				final TypedExpression rightOperand = right.typed(BigInteger.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected comparable expression", right.position()));
				}
				result = parseMultiplicationRight(
						new TypedBinary(leftOperand, operator, rightOperand, BigInteger.class));
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parsePowerLeft() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseUnary();
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parsePowerRight(final Expression left) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final Expression result;
		if (buffer.off() || buffer.current() != '^') {
			result = left;
		} else {
			final TypedExpression leftOperand = left.typed(BigInteger.class);
			if (leftOperand == null) {
				throw new ParserException(error("Expected numeric expression", left.position()));
			}
			final Expression right = parsePowerRight(parsePowerLeft());
			final TypedExpression rightOperand = right.typed(BigInteger.class);
			if (rightOperand == null) {
				throw new ParserException(error("Expected comparable expression", right.position()));
			}
			result = new TypedBinary(leftOperand, Binary.Operator.POWER, rightOperand, BigInteger.class);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseUnary() {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		if (buffer.off()) {
			throw new ParserException(error("Expected expression"));
		}
		final Expression result;
		final int position = buffer.position();
		if (readKeyword("not")) {
			final Expression operand = parseUnary();
			final TypedExpression typedOperand = operand.typed(Boolean.class);
			if (typedOperand == null) {
				throw new ParserException(error("Expected boolean expression", operand.position()));
			}
			result = new TypedUnary(position, Unary.Operator.NOT, typedOperand, Boolean.class);
		} else {
			switch (buffer.current()) {
			case '+': {
				buffer.next();
				final Expression operand = parseUnary();
				final TypedExpression typedOperand = operand.typed(BigInteger.class);
				if (typedOperand == null) {
					throw new ParserException(error("Expected comparable expression", operand.position()));
				}
				result = operand;
				break;
			}
			case '-':
				buffer.next(); {
				final Expression operand = parseUnary();
				final TypedExpression typedOperand = operand.typed(BigInteger.class);
				if (typedOperand == null) {
					throw new ParserException(error("Expected comparable expression", operand.position()));
				}
				result = new TypedUnary(position, Unary.Operator.MINUS, typedOperand, BigInteger.class);
				break;
			}
			default:
				result = parseAtomicExpression();
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAtomicExpression() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression atom;
		if (buffer.off()) {
			throw new ParserException(error("Expected expression"));
		}
		final int position = buffer.position();
		if (buffer.current() == '(') {
			buffer.next();
			atom = parseExpression();
			skipBlanks();
			if (buffer.off() || buffer.current() != ')') {
				throw new ParserException(error("Unfinished expresssion"));
			}
			buffer.next();
		} else {
			switch (buffer.current()) {
			case '"':
				atom = parseString();
				break;
			case '/':
				atom = parseRegex();
				break;
			case '[':
				atom = parseArray();
				break;
			case '{':
				atom = parseDictionary();
				break;
			default:
				if (Character.isDigit(buffer.current())) {
					atom = parseNumber();
				} else if (readKeyword("result")) {
					atom = new Result(position);
				} else {
					final String name = readIdentifier();
					skipBlanks();
					if (buffer.off() || buffer.current() != '(') {
						atom = new Identifier(position, name);
					} else {
						atom = parseFunctionCall(position, name);
					}
				}
			}
		}
		final Expression result = parseIdentifierSuffix(atom);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseIdentifierSuffix(final Expression expression) {
		LOGGER.debug("<-- {}", buffer.position());
		Expression result = expression;
		boolean more = !buffer.off();
		while (more) {
			skipBlanks();
			switch (buffer.current()) {
			case '[':
				result = parseIndexed(result);
				break;
			case '.':
				result = parseDotted(result);
				break;
			default:
				more = false;
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseIndexed(final Expression expression) {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '[';
		buffer.next();
		final Expression index = parseExpression();
		skipBlanks();
		if (buffer.off() || buffer.current() != ']') {
			throw new ParserException(error("Missing closing bracket"));
		}
		buffer.next();
		final TypedExpression typedIndex = index.typed(Comparable.class);
		if (typedIndex == null) {
			throw new ParserException(error("Expected numeric or string index", index.position()));
		}
		final IndexedExpression result = new IndexedExpression(expression, typedIndex);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseDotted(final Expression expression) {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '.';
		buffer.next();
		skipBlanks();
		final int position = buffer.position();
		final String identifier = readIdentifier();
		if (identifier == null) {
			throw new ParserException(error("Expected identifier"));
		}
		final IndexedExpression result = new IndexedExpression(expression, new ManifestString(position, identifier));
		LOGGER.debug("--> {}", result);
		return result;
	}

	private TypedExpression parseString() {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '"';
		final int position = buffer.position();
		buffer.next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (buffer.off()) {
				throw new ParserException(error("Unfinished string", position));
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
				throw new ParserException(error("BUG: unexpected state " + state, position));
			}
			buffer.next();
		} while (state >= 0);
		final ManifestString result = new ManifestString(position, b.toString());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private TypedExpression parseRegex() {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '/';
		final int position = buffer.position();
		buffer.next();
		final StringBuilder b = new StringBuilder();
		int state = 0;
		do {
			if (buffer.off()) {
				throw new ParserException(error("Unfinished string", position));
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
				throw new ParserException(error("BUG: unexpected state " + state, position));
			}
			buffer.next();
		} while (state >= 0);
		final ManifestRegex result = new ManifestRegex(position, Pattern.compile(b.toString()));
		LOGGER.debug("--> {}", result);
		return result;
	}

	private ManifestArray parseArray() {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '[';
		final ManifestArray result = new ManifestArray(buffer.position());
		buffer.next();
		skipBlanks();
		if (buffer.off()) {
			throw new ParserException(error("Invalid array"));
		}
		if (buffer.current() == ']') {
			buffer.next();
		} else {
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
					throw new ParserException(error("Unexpected character"));
				}
			} while (more);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private ManifestDictionary parseDictionary() {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '{';
		final ManifestDictionary result = new ManifestDictionary(buffer.position());
		buffer.next();
		skipBlanks();
		if (buffer.off()) {
			throw new ParserException(error("Invalid array"));
		}
		if (buffer.current() == '}') {
			buffer.next();
		} else {
			boolean more = true;
			do {
				final Expression key = parseExpression();
				skipBlanks();
				if (buffer.off() || buffer.current() != ':') {
					throw new ParserException(error("Invalid dictionary", buffer.position()));
				}
				final TypedExpression typedKey = key.typed(Comparable.class);
				if (typedKey == null) {
					throw new ParserException(error("Invalid dictionary key", key.position()));
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
					throw new ParserException(error("Unexpected character"));
				}
			} while (more);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private TypedExpression parseNumber() {
		LOGGER.debug("<-- {}", buffer.position());
		assert Character.isDigit(buffer.current());
		final int position = buffer.position();
		final StringBuilder b = new StringBuilder();
		while (!buffer.off() && Character.isDigit(buffer.current())) {
			b.append(buffer.current());
			buffer.next();
		}
		final BigInteger value = new BigInteger(b.toString());
		final ManifestNumeric result = new ManifestNumeric(position, value);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private FunctionCall parseFunctionCall(final int position, final String name) {
		LOGGER.debug("<-- {}", buffer.position());
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
				throw new ParserException(error("Unexpected character"));
			}
		} while (more);
		skipBlanks();
		if (buffer.current() == ';') {
			buffer.next();
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Filter parseFilter() {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final Filter result;
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
				throw new ParserException(error("Expected block"));
			}
			final Block instr = parseBlock();
			result = new BoundFilter(position, bound, instr);
		} else {
			buffer.rewind(position);
			final Expression expr = parseExpression();
			final TypedExpression condition = expr.typed(Boolean.class);
			if (condition == null) {
				throw new ParserException(error("Expected boolean condition", expr.position()));
			}
			skipBlanks();
			if (buffer.off() || buffer.current() != '{') {
				throw new ParserException(error("Expected block"));
			}
			final Block instr = parseBlock();
			result = new ConditionFilter(condition, instr);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private String readIdentifier() {
		LOGGER.debug("<-- {}", buffer.position());
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
		LOGGER.debug("--> {}", result);
		return result;
	}

	private String readRawIdentifier() {
		LOGGER.debug("<-- {}", buffer.position());
		final char first = buffer.current();
		if (!Character.isJavaIdentifierStart(first)) {
			LOGGER.debug("--> null");
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
				if (Character.isJavaIdentifierPart(c)) {
					result.append(c);
					buffer.next();
				} else {
					more = false;
				}
			}
		} while (more);
		LOGGER.debug("--> {}", result);
		return result.toString();
	}

	private boolean isReserved(final String identifier) {
		switch (identifier) {
		case "and":
		case "at":
		case "case":
		case "def":
		case "default":
		case "do":
		case "else":
		case "emit":
		case "if":
		case "next":
		case "not":
		case "or":
		case "result":
		case "switch":
		case "while":
		case "xor":
			return true;
		default:
			return false;
		}
	}

	private boolean readKeyword(final String keyword) {
		LOGGER.debug("<-- {} at {}", keyword, buffer.position());
		skipBlanks();
		final int position = buffer.position();
		for (final char kw : keyword.toCharArray()) {
			if (buffer.off() || buffer.current() != kw) {
				buffer.rewind(position);
				LOGGER.debug("--> false");
				return false;
			}
			buffer.next();
		}
		if (!(buffer.off() || !Character.isJavaIdentifierPart(buffer.current()))) {
			buffer.rewind(position);
			LOGGER.debug("--> false");
			return false;
		}
		LOGGER.debug("--> true");
		return true;
	}

	private void skipBlanks() {
		LOGGER.debug("<-- {}", error("skip"));
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
					LOGGER.debug("--> {}", error("skip"));
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

	private String error(final String message) {
		return buffer.error(message);
	}

	public String error(final String message, final int position) {
		return buffer.error(message, position);
	}

	public String error(final String message, final int... positions) {
		return buffer.error(message, positions);
	}

}
