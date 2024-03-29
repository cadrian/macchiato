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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.ast.expression.ManifestBoolean;
import net.cadrian.macchiato.ruleset.parser.Position;

public class If implements Instruction {

	private static final Logger LOGGER = LoggerFactory.getLogger(If.class);

	public static interface Visitor extends Node.Visitor {
		void visitIf(If i);
	}

	private final Position position;
	private final Expression condition;
	private final Instruction instruction;
	private final Instruction otherwise;

	public If(final Position position, final Expression condition, final Instruction instruction,
			final Instruction otherwise) {
		this.position = position;
		this.condition = condition;
		this.instruction = instruction;
		this.otherwise = otherwise == null ? DoNothing.instance : otherwise;
	}

	public Expression getCondition() {
		return condition;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public Instruction getOtherwise() {
		return otherwise;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitIf(this);
	}

	@Override
	public Instruction simplify() {
		final Expression simplifyCondition = condition.simplify();
		final Instruction simplifyInstruction = instruction.simplify();
		final Instruction simplifyOtherwise = otherwise == null ? null : otherwise.simplify();
		If result;
		if (simplifyCondition == condition && simplifyInstruction == instruction && simplifyOtherwise == otherwise) {
			result = this;
		} else {
			result = new If(position, simplifyCondition, simplifyInstruction, simplifyOtherwise);
		}
		if (simplifyCondition.isStatic()) {
			final ManifestBoolean cond = (ManifestBoolean) simplifyCondition.getStaticValue();
			if (Boolean.TRUE.equals(cond.getValue())) {
				LOGGER.debug("replace if-else by always-true");
				return simplifyInstruction;
			} else {
				LOGGER.debug("replace if-else by always-false");
				return simplifyOtherwise;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "{If " + condition + ": " + instruction + " Else " + otherwise + "}";
	}

}
