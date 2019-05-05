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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.conf.Platform;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.interpreter.objects.MacComparable;
import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacPattern;
import net.cadrian.macchiato.interpreter.objects.MacString;
import net.cadrian.macchiato.interpreter.objects.container.MacContainer;
import net.cadrian.macchiato.midi.Message;
import net.cadrian.macchiato.ruleset.ast.BoundFilter;
import net.cadrian.macchiato.ruleset.ast.ConditionFilter;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Filter;
import net.cadrian.macchiato.ruleset.ast.FormalArgs;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Binary;
import net.cadrian.macchiato.ruleset.ast.expression.Binary.Operator;
import net.cadrian.macchiato.ruleset.ast.expression.ExistsExpression;
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
import net.cadrian.macchiato.ruleset.ast.instruction.Local;
import net.cadrian.macchiato.ruleset.ast.instruction.Next;
import net.cadrian.macchiato.ruleset.ast.instruction.ProcedureCall;
import net.cadrian.macchiato.ruleset.ast.instruction.While;

public class Parser {

	private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

	private final File relativeDirectory;
	private final ParserBuffer buffer;

	private boolean inDef;

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
				buffer.skipBlanks();
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
				buffer.skipBlanks();
			}
		} catch (final Exception e) {
			throw new ParserException(error(e.getMessage()), e);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private void parseImport(final Ruleset result, final int p) {
		LOGGER.debug("<--");
		buffer.skipBlanks();
		final String name = readIdentifier();

		buffer.skipBlanks();
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
		buffer.skipBlanks();
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
		inDef = true;
		buffer.skipBlanks();
		final String name = readIdentifier();
		if (name == null) {
			throw new ParserException(error("Expected def name"));
		}
		final FormalArgs args = parseFormalArgs();
		buffer.skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(error("Expected block"));
		}
		final Block inst = parseBlock();
		final Def result = new Def(position, name, args, inst);
		inDef = false;
		LOGGER.debug("--> {}", result);
		return result;
	}

	private FormalArgs parseFormalArgs() {
		LOGGER.debug("<-- {}", buffer.position());
		buffer.skipBlanks();
		if (buffer.current() != '(') {
			throw new ParserException(error("Expected formal arguments"));
		}
		final FormalArgs result = new FormalArgs();
		buffer.next();
		buffer.skipBlanks();
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
					buffer.skipBlanks();
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
		buffer.skipBlanks();
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
			case "local": {
				final Local result = parseLocal(position);
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
			buffer.skipBlanks();
			final String scopedName = parseScopedCallName(name);
			if (scopedName != null) {
				assert !buffer.off() && buffer.current() == '(';
				final ProcedureCall result = parseProcedureCall(position, scopedName);
				LOGGER.debug("--> {}", result);
				return result;
			}
			indexable = new Identifier(position, name);
		}
		buffer.skipBlanks();
		switch (buffer.current()) {
		case '[':
		case '.': {
			final Expression indexed = parseIdentifierSuffix(indexable);
			buffer.skipBlanks();
			if (buffer.off() || buffer.current() != '=') {
				throw new ParserException(error("Expected assignment"));
			}
			buffer.next();
			final Expression exp = parseExpression();
			buffer.skipBlanks();
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
			buffer.skipBlanks();
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

	private Local parseLocal(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		if (!inDef) {
			throw new ParserException(error("Unexpected local out of def", position));
		}
		final String localId = readIdentifier();
		if (localId == null) {
			throw new ParserException(error("Expected indentifier"));
		}
		final Identifier local = new Identifier(position, localId);
		buffer.skipBlanks();
		final Expression initializer;
		if (buffer.off() || buffer.current() != '=') {
			initializer = null;
		} else {
			buffer.next();
			initializer = parseExpression();
			if (initializer == null) {
				throw new ParserException(error("Expected expression"));
			}
		}
		buffer.skipBlanks();
		if (!buffer.off() && buffer.current() == ';') {
			buffer.next();
		}
		final Local result = new Local(position, local, initializer);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Next parseNext(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		buffer.skipBlanks();
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
		buffer.skipBlanks();
		if (buffer.off() || buffer.current() == ';') {
			result = new Emit(position, null, null);
		} else {
			final Expression expression = parseExpression();
			final TypedExpression messageExpression = expression.typed(Message.class);
			final TypedExpression tickExpression;
			buffer.skipBlanks();
			if (readKeyword("at")) {
				final Expression te = parseExpression();
				tickExpression = te.typed(MacNumber.class);
			} else {
				tickExpression = null;
			}
			result = new Emit(position, messageExpression, tickExpression);
		}
		buffer.skipBlanks();
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
		buffer.skipBlanks();
		if (buffer.off()) {
			throw new ParserException("Invalid arguments list");
		}
		if (buffer.current() == ')') {
			buffer.next();
		} else {
			boolean more = true;
			do {
				final Expression exp = parseExpression();
				result.add(exp);
				buffer.skipBlanks();
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
		}
		buffer.skipBlanks();
		if (buffer.current() == ';') {
			buffer.next();
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private For parseFor(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		buffer.skipBlanks();
		final int p1 = buffer.position();
		final String id1 = readIdentifier();
		if (id1 == null) {
			throw new ParserException(error("Expected identifier"));
		}
		final Expression name1 = new Identifier(p1, id1);

		buffer.skipBlanks();
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

		buffer.skipBlanks();
		final int p3 = buffer.position();
		final Expression loop = parseExpression().typed(MacContainer.class);
		if (loop == null) {
			throw new ParserException(error("Invalid expression", p3));
		}

		buffer.skipBlanks();
		final Instruction instruction = parseBlock();

		final For result = new For(position, name1, name2, loop, instruction);
		LOGGER.debug("--> {}", result);
		return result;
	}

	private If parseIf(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		final Expression cond = parseExpression();
		buffer.skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(error("Expected block"));
		}
		final Instruction instruction = parseBlock();
		final Instruction otherwise;
		buffer.skipBlanks();
		if (readKeyword("else")) {
			buffer.skipBlanks();
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
		buffer.skipBlanks();
		if (buffer.off() || buffer.current() != '{') {
			throw new ParserException(error("Expected block"));
		}
		final Block instruction = parseBlock();
		final Block otherwise;
		buffer.skipBlanks();
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
			buffer.skipBlanks();
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
		final TypedExpression leftOperand = left.typed(MacBoolean.class);
		if (leftOperand == null) {
			throw new ParserException(error("Expected boolean expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression rightOperand = right.typed(MacBoolean.class);
		if (rightOperand == null) {
			throw new ParserException(error("Expected boolean expression", right.position()));
		}
		result = parseOrRight(new TypedBinary(leftOperand, operator, rightOperand, MacBoolean.class));
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
		final TypedExpression leftOperand = left.typed(MacBoolean.class);
		if (leftOperand == null) {
			throw new ParserException(error("Expected boolean expression", left.position()));
		}
		final Expression right = parseExpression();
		final TypedExpression rightOperand = right.typed(MacBoolean.class);
		if (rightOperand == null) {
			throw new ParserException(error("Expected boolean expression", right.position()));
		}
		result = parseAndRight(new TypedBinary(leftOperand, operator, rightOperand, MacBoolean.class));
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
				final TypedExpression leftOperand = left.typed(MacString.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected string", left.position()));
				}
				final TypedExpression rightOperand = right.typed(MacPattern.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected regular expression", right.position()));
				}
				result = parseComparatorRight(
						new TypedBinary(leftOperand, Binary.Operator.MATCH, rightOperand, MacBoolean.class));
			} else {
				final TypedExpression leftOperand = left.typed(MacComparable.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected comparable expression", left.position()));
				}
				final TypedExpression rightOperand = right.typed(MacComparable.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected comparable expression", right.position()));
				}
				result = parseComparatorRight(new TypedBinary(leftOperand, comparator, rightOperand, MacBoolean.class));
			}
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Binary.Operator readComparator() {
		buffer.skipBlanks();
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
				buffer.next();
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
		buffer.skipBlanks();
		final Expression result;
		if (buffer.off()) {
			result = left;
		} else {
			switch (buffer.current()) {
			case '+': {
				buffer.next();
				final TypedExpression leftOperand = left.typed(MacComparable.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected comparable expression", left.position()));
				}
				final Expression right = parseExpression();
				final TypedExpression rightOperand = right.typed(MacComparable.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected comparable expression", right.position()));
				}
				@SuppressWarnings("rawtypes")
				final Class<MacComparable> resultType = MacComparable.class;
				result = parseAdditionRight(
						new TypedBinary(leftOperand, Binary.Operator.ADD, rightOperand, resultType));
				break;
			}
			case '-': {
				buffer.next();
				final TypedExpression leftOperand = left.typed(MacNumber.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected numeric expression", left.position()));
				}
				final Expression right = parseExpression();
				final TypedExpression rightOperand = right.typed(MacNumber.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected numeric expression", right.position()));
				}
				result = parseAdditionRight(
						new TypedBinary(leftOperand, Binary.Operator.SUBTRACT, rightOperand, MacNumber.class));
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
		buffer.skipBlanks();
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
				final TypedExpression leftOperand = left.typed(MacNumber.class);
				if (leftOperand == null) {
					throw new ParserException(error("Expected comparable expression", left.position()));
				}
				final Expression right = parseExpression();
				final TypedExpression rightOperand = right.typed(MacNumber.class);
				if (rightOperand == null) {
					throw new ParserException(error("Expected comparable expression", right.position()));
				}
				result = parseMultiplicationRight(
						new TypedBinary(leftOperand, operator, rightOperand, MacNumber.class));
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
		buffer.skipBlanks();
		final Expression result;
		if (buffer.off() || buffer.current() != '^') {
			result = left;
		} else {
			final TypedExpression leftOperand = left.typed(MacNumber.class);
			if (leftOperand == null) {
				throw new ParserException(error("Expected numeric expression", left.position()));
			}
			final Expression right = parsePowerRight(parsePowerLeft());
			final TypedExpression rightOperand = right.typed(MacNumber.class);
			if (rightOperand == null) {
				throw new ParserException(error("Expected comparable expression", right.position()));
			}
			result = new TypedBinary(leftOperand, Binary.Operator.POWER, rightOperand, MacNumber.class);
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Expression parseUnary() {
		LOGGER.debug("<-- {}", buffer.position());
		buffer.skipBlanks();
		if (buffer.off()) {
			throw new ParserException(error("Expected expression"));
		}
		final Expression result;
		final int position = buffer.position();
		if (readKeyword("not")) {
			final Expression operand = parseUnary();
			final TypedExpression typedOperand = operand.typed(MacBoolean.class);
			if (typedOperand == null) {
				throw new ParserException(error("Expected boolean expression", operand.position()));
			}
			result = new TypedUnary(position, Unary.Operator.NOT, typedOperand, MacBoolean.class);
		} else {
			switch (buffer.current()) {
			case '+': {
				buffer.next();
				final Expression operand = parseUnary();
				final TypedExpression typedOperand = operand.typed(MacNumber.class);
				if (typedOperand == null) {
					throw new ParserException(error("Expected comparable expression", operand.position()));
				}
				result = operand;
				break;
			}
			case '-':
				buffer.next(); {
				final Expression operand = parseUnary();
				final TypedExpression typedOperand = operand.typed(MacNumber.class);
				if (typedOperand == null) {
					throw new ParserException(error("Expected comparable expression", operand.position()));
				}
				result = new TypedUnary(position, Unary.Operator.MINUS, typedOperand, MacNumber.class);
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
		Expression result = parseIdentifierSuffix(parseAtomicExpression());
		buffer.skipBlanks();
		final int position = buffer.position();
		if (readKeyword("exists")) {
			result = new ExistsExpression(position, result);
		}
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
			buffer.skipBlanks();
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
					if (name == null) {
						throw new ParserException(error("Expected identifier"));
					}
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
							throw new ParserException(error("Unexpected keyword " + name, position));
						}
						buffer.skipBlanks();
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
			buffer.skipBlanks();
			if (buffer.off()) {
				more = false;
			} else {
				switch (buffer.current()) {
				case '.':
					scopedName.append('.');
					buffer.next();
					buffer.skipBlanks();
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
			buffer.skipBlanks();
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
		buffer.skipBlanks();
		if (buffer.off() || buffer.current() != ']') {
			throw new ParserException(error("Missing closing bracket"));
		}
		buffer.next();
		final TypedExpression typedIndex = index.typed(MacComparable.class);
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
		buffer.skipBlanks();
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
		final int position = buffer.position();
		final ManifestString result = new ManifestString(position, buffer.readString());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private ManifestRegex parseManifestRegex() {
		LOGGER.debug("<-- {}", buffer.position());
		final int position = buffer.position();
		final ManifestRegex result = new ManifestRegex(position, buffer.readRegex());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private ManifestArray parseManifestArray() {
		LOGGER.debug("<-- {}", buffer.position());
		assert buffer.current() == '[';
		final ManifestArray result = new ManifestArray(buffer.position());
		buffer.next();
		buffer.skipBlanks();
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
				buffer.skipBlanks();
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
		buffer.skipBlanks();
		if (buffer.off()) {
			throw new ParserException(error("Invalid array"));
		}
		if (buffer.current() == '}') {
			buffer.next();
		} else {
			boolean more = true;
			do {
				final Expression key = parseExpression();
				buffer.skipBlanks();
				if (buffer.off() || buffer.current() != ':') {
					throw new ParserException(error("Invalid dictionary", buffer.position()));
				}
				buffer.next();
				final TypedExpression typedKey = key.typed(MacComparable.class);
				if (typedKey == null) {
					throw new ParserException(error("Invalid dictionary key", key.position()));
				}
				final Expression exp = parseExpression();
				result.put(typedKey, exp);
				buffer.skipBlanks();
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
		final int position = buffer.position();
		final ManifestNumeric result = new ManifestNumeric(position, buffer.readBigInteger());
		LOGGER.debug("--> {}", result);
		return result;
	}

	private FunctionCall parseFunctionCall(final int position, final String name) {
		LOGGER.debug("<-- {}", buffer.position());
		final FunctionCall result = new FunctionCall(position, name);
		assert buffer.current() == '(';
		buffer.next();
		buffer.skipBlanks();
		if (buffer.off()) {
			throw new ParserException("Invalid arguments list");
		}
		if (buffer.current() == ')') {
			buffer.next();
		} else {
			boolean more = true;
			do {
				final Expression exp = parseExpression();
				result.add(exp);
				buffer.skipBlanks();
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
		}
		buffer.skipBlanks();
		if (buffer.current() == ';') {
			buffer.next();
		}
		LOGGER.debug("--> {}", result);
		return result;
	}

	private Filter parseFilter(final int position) {
		LOGGER.debug("<-- {}", buffer.position());
		buffer.skipBlanks();
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
			buffer.skipBlanks();
			if (buffer.off() || buffer.current() != '{') {
				throw new ParserException(error("Expected block"));
			}
			final Instruction instruction = parseBlock();
			result = new BoundFilter(position, bound, instruction);
		} else {
			buffer.rewind(p1);
			final Expression expr = parseExpression();
			final TypedExpression condition = expr.typed(MacBoolean.class);
			if (condition == null) {
				throw new ParserException(error("Expected boolean condition", expr.position()));
			}
			buffer.skipBlanks();
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
		buffer.skipBlanks();
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
		case "exists":
		case "false":
		case "for":
		case "if":
		case "import":
		case "in":
		case "local":
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
		final boolean result = buffer.readKeyword(keyword);
		LOGGER.debug("--> {}", result);
		return result;
	}

	public static void main(final String[] args) throws IOException {
		final File file = new File(args[0]);
		final FileReader fileReader = new FileReader(file);
		final Parser parser = new Parser(file.getParentFile(), fileReader);
		final Ruleset ruleset = parser.parse();
		System.out.println("Ruleset: " + ruleset);
	}

	String error(final String message) {
		return buffer.error(message);
	}

	public String error(final String message, final int position) {
		return buffer.error(message, position);
	}

	public String error(final String message, final int... positions) {
		return buffer.error(message, positions);
	}

	public String error(final InterpreterException e) {
		final StringBuilder result = new StringBuilder();
		fillError(e, result);
		return result.toString();
	}

	private void fillError(final InterpreterException e, final StringBuilder error) {
		error.append(buffer.error(e.getMessage(), e.getPosition())).append('\n');
		final Throwable cause = e.getCause();
		if (cause instanceof InterpreterException) {
			error.append("Caused by:\n");
			fillError((InterpreterException) cause, error);
		}
	}

}
