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
package net.cadrian.macchiato.ruleset.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.conf.Platform;
import net.cadrian.macchiato.interpreter.Container;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.BoundFilter;
import net.cadrian.macchiato.ruleset.ast.ConditionFilter;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Filter;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.RegexMatcher;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Binary;
import net.cadrian.macchiato.ruleset.ast.expression.Binary.Operator;
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
import net.cadrian.macchiato.ruleset.ast.expression.TypedExpression;
import net.cadrian.macchiato.ruleset.ast.expression.TypedUnary;
import net.cadrian.macchiato.ruleset.ast.expression.Unary;
import net.cadrian.macchiato.ruleset.ast.instruction.Assignment;
import net.cadrian.macchiato.ruleset.ast.instruction.Block;
import net.cadrian.macchiato.ruleset.ast.instruction.Emit;
import net.cadrian.macchiato.ruleset.ast.instruction.For;
import net.cadrian.macchiato.ruleset.ast.instruction.If;
import net.cadrian.macchiato.ruleset.ast.instruction.Next;
import net.cadrian.macchiato.ruleset.ast.instruction.ProcedureCall;
import net.cadrian.macchiato.ruleset.ast.instruction.While;

public class Parser {

	private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

	private final File relativeDirectory;
	private final ParserBuffer buffer;

	public Parser(final File relativeDirectory, final Reader reader) throws IOException {
		this.relativeDirectory = relativeDirectory;
		this.buffer = new ParserBuffer(reader);
	}

	public Ruleset parse() {
		return parse(0);
	}

	Ruleset parse(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		final Ruleset result = new Ruleset(position);
		try {
			boolean allowImport = true;
			while (!buffer.off()) {
				skipBlanks();
				final int p = buffer.position();
				if (readKeyword("import")) {
					if (!allowImport) {
						throw new ParserException(error("Cannot import files after filters", p));
					}
					parseImport(result, p);
				} else if (readKeyword("def")) {
					final Def def = parseDef(p);
					final Def old = result.addDef(def);
					if (old != null) {
						throw new ParserException(error("Duplicate def " + old.name(), old.position(), def.position()));
					}
				} else {
					result.addFilter(parseFilter(p));
					allowImport = false;
				}
				skipBlanks();
			}
		} catch (final Exception e) {
			throw new ParserException(error(e.getMessage()), e);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private void parseImport(final Ruleset result, final int p) {
		LOGGER.debug("<--");
		skipBlanks();
		final String name = readIdentifier();

		skipBlanks();
		final int p1 = buffer.position();
		final ManifestString scopePath = parseManifestString();
		final File scopeFile = findFile(p1, scopePath.getValue());
		if (scopeFile == null) {
			throw new ParserException(error("Could not import scope " + name + " from " + scopePath.getValue()
					+ ": file not found (relative directory: " + relativeDirectory.getPath() + ")", p1));
		}
		LOGGER.debug("Found {} at {}", scopePath.getValue(), scopeFile.getAbsolutePath());

		final Ruleset scope;
		try (final FileReader scopeReader = new FileReader(scopeFile)) {
			final Parser scopeParser = new Parser(scopeFile.getParentFile(), scopeReader);
			try {
				scope = scopeParser.parse(p);
			} catch (final ParserException e) {
				throw new ParserException(error("In " + scopePath.getValue(), p1) + '\n' + e.getMessage(), e);
			}
		} catch (final IOException e) {
			throw new ParserException(error("Could read " + scopeFile.getPath(), p1), e);
		}
		final Ruleset old = result.addScope(name, scope);
		if (old != null) {
			throw new ParserException(error("Duplicate scope " + name, old.position(), scope.position()));
		}
		skipBlanks();
		if (!buffer.off() && buffer.current() == ';') {
			buffer.next();
		}

		LOGGER.debug("--> {}", scope);
	}

	private File findFile(final int position, final String scopePath) {
		if (scopePath.isEmpty()) {
			throw new ParserException(error("empty path", position));
		}

		final File raw = new File(scopePath);
		if (raw.isAbsolute()) {
			if (raw.exists()) {
				return raw;
			}
			return null;
		}

		final File relative = new File(relativeDirectory, scopePath);
		if (relative.exists()) {
			return relative;
		}

		return Platform.getConfigFile(scopePath);
	}

	private Def parseDef(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final String name = readIdentifier();
		if (name == null) {
			throw new ParserException(error("Expected def name"));
		}
		final FormalArgs args = parseFormalArgs();
		skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(error("Expected block"));
		}
		final Block inst = parseBlock();
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
		final Expression indexable;
		if (isReserved(name)) {
			switch (name) {
			case "do":
				// TODO return parseDo();
				throw new ParserException(error("not yet implemented"));
			case "emit": {
				final Emit result = parseEmit(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "for": {
				final For result = parseFor(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "if": {
				final If result = parseIf(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "next": {
				final Next result = parseNext(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			case "result": {
				indexable = new Result(position);
				break;
			}
			case "switch":
				// TODO return parseSwitch();
				throw new ParserException(error("not yet implemented"));
			case "while": {
				final While result = parseWhile(position);
				LOGGER.debug("--> {}", result);
				return result;
			}
			default:
				throw new ParserException(error("Unexpected keyword " + name, position));
			}
		} else {
			skipBlanks();
			final String scopedName = parseScopedCallName(name);
			if (scopedName != null) {
				assert !buffer.off() && buffer.current() == '(';
				final ProcedureCall result = parseProcedureCall(position, scopedName);
				LOGGER.debug("--> {}", result);
				return result;
			}
			indexable = new Identifier(position, name);
		}
		skipBlanks();
		switch (buffer.current()) {
		case '[':
		case '.': {
			final Expression indexed = parseIdentifierSuffix(indexable);
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
			final Assignment result = new Assignment(indexable, exp);
			LOGGER.debug("--> {}", result);
			return result;
		}
		default:
			throw new ParserException(error("Expected assignment or function call"));
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

	private For parseFor(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final int p1 = buffer.position();
		final String id1 = readIdentifier();
		if (id1 == null) {
			throw new ParserException(error("Expected identifier"));
		}
		final Expression name1 = new Identifier(p1, id1);

		skipBlanks();
		final int p2 = buffer.position();
		final Expression name2;
		if (buffer.off()) {
			throw new ParserException(error("Unexpected end of file"));
		}
		if (buffer.current() == ',') {
			buffer.next();
			final String id2 = readIdentifier();
			if (id2 == null) {
				throw new ParserException(error("Expected identifier"));
			}
			name2 = new Identifier(p2, id2);
		} else {
			name2 = null;
		}

		if (!readKeyword("in")) {
			throw new ParserException(error("Expected \"in\""));
		}

		skipBlanks();
		final int p3 = buffer.position();
		final Expression loop = parseExpression().typed(Container.class);
		if (loop == null) {
			throw new ParserException(error("Invalid expression", p3));
		}

		skipBlanks();
		final Instruction instruction = parseBlock();

		final For result = new For(position, name1, name2, loop, instruction);
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
		final Instruction instruction = parseBlock();
		final Instruction otherwise;
		skipBlanks();
		if (readKeyword("else")) {
			skipBlanks();
			final int pos = buffer.position();
			if (readKeyword("if")) {
				otherwise = parseIf(pos);
			} else {
				otherwise = parseBlock();
			}
		} else {
			otherwise = null;
		}
		final If result = new If(position, cond, instruction, otherwise);
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
		final Block instruction = parseBlock();
		final Block otherwise;
		skipBlanks();
		if (readKeyword("else")) {
			otherwise = parseBlock();
		} else {
			otherwise = null;
		}
		final While result = new While(position, cond, instruction, otherwise);
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
				LOGGER.debug("end of block");
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
		if (readKeyword("or")) {
			result = parseOrRight(left, Binary.Operator.OR);
		} else if (readKeyword("xor")) {
			result = parseOrRight(left, Binary.Operator.XOR);
		} else {
			result = left;
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseOrRight(final Expression left, final Operator operator) {
		final Expression result;
		final TypedExpression leftOperand = left.typed(Boolean.class);
		if (leftOperand == null) {
			throw new ParserException(error("Expected boolean expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression rightOperand = right.typed(Boolean.class);
		if (rightOperand == null) {
			throw new ParserException(error("Expected boolean expression", right.position()));
		}
		result = parseOrRight(new TypedBinary(leftOperand, operator, rightOperand, Boolean.class));
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
		if (readKeyword("and")) {
			result = parseAndRight(left, Binary.Operator.AND);
		} else {
			result = left;
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAndRight(final Expression left, final Operator operator) {
		final Expression result;
		final TypedExpression leftOperand = left.typed(Boolean.class);
		if (leftOperand == null) {
			throw new ParserException(error("Expected boolean expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression rightOperand = right.typed(Boolean.class);
		if (rightOperand == null) {
			throw new ParserException(error("Expected boolean expression", right.position()));
		}
		result = parseAndRight(new TypedBinary(leftOperand, operator, rightOperand, Boolean.class));
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
				@SuppressWarnings("rawtypes")
				final Class<Comparable> resultType = Comparable.class;
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
				result = parseAtomicExpressionWithSuffix();
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAtomicExpressionWithSuffix() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result = parseIdentifierSuffix(parseAtomicExpression());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseAtomicExpression() {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression result;
		if (buffer.off()) {
			throw new ParserException(error("Expected expression"));
		}
		final int position = buffer.position();
		if (buffer.current() == '(') {
			buffer.next();
			result = parseExpression();
			skipBlanks();
			if (buffer.off() || buffer.current() != ')') {
				throw new ParserException(error("Unfinished expresssion"));
			}
			buffer.next();
		} else {
			switch (buffer.current()) {
			case '"':
				result = parseManifestString();
				break;
			case '/':
				result = parseManifestRegex();
				break;
			case '[':
				result = parseManifestArray();
				break;
			case '{':
				result = parseManifestDictionary();
				break;
			default:
				if (Character.isDigit(buffer.current())) {
					result = parseManifestNumber();
				} else {
					final String name = readRawIdentifier();
					switch (name) {
					case "result":
						result = new Result(position);
						break;
					case "true":
						result = new ManifestBoolean(position, true);
						break;
					case "false":
						result = new ManifestBoolean(position, false);
						break;
					default:
						if (isReserved(name)) {
							throw new ParserException("unexpected keyword " + name);
						}
						skipBlanks();
						final String scopedName = parseScopedCallName(name);
						if (scopedName != null) {
							assert !buffer.off() && buffer.current() == '(';
							result = parseFunctionCall(position, scopedName);
						} else {
							result = new Identifier(position, name);
						}
					}
				}
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private String parseScopedCallName(final String name) {
		LOGGER.debug("<--");
		final String result;
		final int positionAfterFirstName = buffer.position();
		final StringBuilder scopedName = new StringBuilder(name);
		boolean foundParenthesis = !buffer.off() && buffer.current() == '(';
		boolean more = !buffer.off() && !foundParenthesis;
		while (more) {
			skipBlanks();
			if (buffer.off()) {
				more = false;
			} else {
				switch (buffer.current()) {
				case '.':
					scopedName.append('.');
					buffer.next();
					skipBlanks();
					final int p0 = buffer.position();
					final String subname = readIdentifier();
					if (subname == null) {
						throw new ParserException(error("Expected identifier", p0));
					}
					scopedName.append(subname);
					break;
				case '(':
					foundParenthesis = true;
					more = false;
					break;
				default:
					more = false;
				}
			}
		}
		if (foundParenthesis) {
			assert !buffer.off() && buffer.current() == '(';
			result = scopedName.toString();
		} else {
			buffer.rewind(positionAfterFirstName);
			result = null;
		}
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

	private ManifestString parseManifestString() {
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

	private ManifestRegex parseManifestRegex() {
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

	private ManifestArray parseManifestArray() {
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

	private ManifestDictionary parseManifestDictionary() {
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

	private ManifestNumeric parseManifestNumber() {
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

	private Filter parseFilter(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		skipBlanks();
		final Filter result;
		final int p1 = buffer.position();
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
			final Instruction instruction = parseBlock();
			result = new BoundFilter(position, bound, instruction);
		} else {
			buffer.rewind(p1);
			final Expression expr = parseExpression();
			final TypedExpression condition = expr.typed(Boolean.class);
			if (condition == null) {
				throw new ParserException(error("Expected boolean condition", expr.position()));
			}
			skipBlanks();
			if (buffer.off() || buffer.current() != '{') {
				throw new ParserException(error("Expected block"));
			}
			final Instruction instruction = parseBlock();
			result = new ConditionFilter(position, condition, instruction);
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
		case "false":
		case "for":
		case "if":
		case "import":
		case "in":
		case "next":
		case "not":
		case "or":
		case "result":
		case "switch":
		case "true":
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
		int pos = 0;
		while (!buffer.off()) {
			switch (state) {
			case 0: // normal text
				switch (buffer.current()) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break;
				case '#':
					state = 10;
					break;
				case '/':
					pos = buffer.position();
					state = 1;
					break;
				default:
					LOGGER.debug("--> {}", error("skip"));
					return;
				}
				break;
			case 1: // after '/'
				switch (buffer.current()) {
				case '/':
					state = 10;
					break;
				case '*':
					state = 20;
					break;
				default:
					buffer.rewind(pos);
					LOGGER.debug("--> {}", error("skip"));
					return;
				}
				break;
			case 10: // in one-line comment
				switch (buffer.current()) {
				case '\r':
				case '\n':
					state = 0;
					break;
				}
				break;
			case 20: // block comment
				switch (buffer.current()) {
				case '*':
					state = 21;
					break;
				default:
					break;
				}
				break;
			case 21: // block comment after '*'
				switch (buffer.current()) {
				case '/':
					state = 0;
					break;
				default:
					state = 20;
					break;
				}
				break;
			default:
				throw new ParserException("BUG: invalid state " + state);
			}
			buffer.next();
		}
	}

	public static void main(final String[] args) throws IOException {
		final File file = new File(args[0]);
		final FileReader fileReader = new FileReader(file);
		final Parser parser = new Parser(file.getParentFile(), fileReader);
		final Ruleset ruleset = parser.parse();
		System.out.println("Ruleset: " + ruleset);
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
