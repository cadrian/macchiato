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
package net.cadrian.macchiato.ruleset.ast.instruction;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestArray;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestDictionary;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestDictionary.Entry;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestNumeric;
import net.cadrian.macchiato.ruleset.parser.Position;

public class For implements Instruction {

	private static final Logger LOGGER = LoggerFactory.getLogger(For.class);

	public interface Visitor extends Node.Visitor {
		void visitFor(For f);
	}

	private final Position position;
	private final Expression name1;
	private final Expression name2;
	private final Expression loop;
	private final Instruction instruction;

	public For(final Position position, final Expression name1, final Expression name2, final Expression loop,
			final Instruction instruction) {
		this.position = position;
		this.name1 = name1;
		this.name2 = name2;
		this.loop = loop;
		this.instruction = instruction;
	}

	public Expression getName1() {
		return name1;
	}

	public Expression getName2() {
		return name2;
	}

	public Expression getLoop() {
		return loop;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitFor(this);
	}

	@Override
	public Instruction simplify() {
		final Expression simplifyName1 = name1.simplify();
		final Expression simplifyName2 = name2 == null ? null : name2.simplify();
		final Expression simplifyLoop = loop.simplify();
		final Instruction simplifyInstruction = instruction.simplify();
		if (simplifyInstruction == DoNothing.instance) {
			return DoNothing.instance;
		}
		final For result;
		if (simplifyName1 == name1 && simplifyName2 == name2 && simplifyLoop == loop
				&& simplifyInstruction == instruction) {
			result = this;
		} else {
			result = new For(position, simplifyName1, simplifyName2, simplifyLoop, simplifyInstruction);
		}
		if (simplifyLoop.isStatic()) {
			final Expression container = simplifyLoop.getStaticValue();
			if (container instanceof ManifestArray) {
				return simplifyManifestArray(simplifyName1, simplifyName2, simplifyInstruction, result, container);
			} else if (container instanceof ManifestDictionary) {
				return simplifyManifestDictionary(simplifyName1, simplifyName2, simplifyInstruction, result, container);
			}
		}
		return result;
	}

	private Instruction simplifyManifestArray(final Expression simplifyName1, final Expression simplifyName2,
			final Instruction simplifyInstruction, final For result, final Expression container) {
		final List<Expression> expressions = ((ManifestArray) container).getExpressions();
		switch (expressions.size()) {
		case 0:
			LOGGER.debug("remove empty loop");
			return DoNothing.instance;
		case 1:
			LOGGER.debug("replace one-run loop by block");
			final Block b = new Block(position);
			final Expression firstExpression = expressions.get(0);
			if (name2 == null) {
				b.add(new Assignment(simplifyName1, firstExpression));
			} else {
				b.add(new Assignment(simplifyName1, new ManifestNumeric(simplifyName1.position(), BigInteger.ZERO)));
				b.add(new Assignment(simplifyName2, firstExpression));
			}
			b.add(simplifyInstruction);
			return b;
		default:
			return result;
		}
	}

	private Instruction simplifyManifestDictionary(final Expression simplifyName1, final Expression simplifyName2,
			final Instruction simplifyInstruction, final For result, final Expression container) {
		final ManifestDictionary dictionary = (ManifestDictionary) container;
		final List<Entry> expressions = dictionary.getExpressions();
		switch (expressions.size()) {
		case 0:
			LOGGER.debug("remove empty loop");
			return DoNothing.instance;
		case 1:
			LOGGER.debug("replace one-run loop by block");
			final Block b = new Block(position);
			final Entry firstExpression = expressions.get(0);
			if (name2 == null) {
				b.add(new Assignment(simplifyName1, firstExpression.getExpression()));
			} else {
				b.add(new Assignment(simplifyName1, firstExpression.getKey()));
				b.add(new Assignment(simplifyName2, firstExpression.getExpression()));
			}
			b.add(simplifyInstruction);
			return b;
		default:
			return result;
		}
	}

	@Override
	public String toString() {
		return "{For " + name1 + (name2 == null ? "" : ", " + name2) + " in " + loop + " do " + instruction + "}";
	}

}
