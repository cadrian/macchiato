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

public class While implements Instruction {

	private static final Logger LOGGER = LoggerFactory.getLogger(While.class);

	public static interface Visitor extends Node.Visitor {
		void visitWhile(While w);
	}

	private final int position;
	private final Expression condition;
	private final Instruction instruction;
	private final Instruction otherwise;

	public While(final int position, final Expression condition, final Instruction instruction,
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
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitWhile(this);
	}

	@Override
	public Instruction simplify() {
		final Expression simplifyCondition = condition.simplify();
		final Instruction simplifyInstruction = instruction.simplify();
		final Instruction simplifyOtherwise = otherwise == null ? null : otherwise.simplify();
		While result;
		if (simplifyCondition == condition && simplifyInstruction == instruction && simplifyOtherwise == otherwise) {
			result = this;
		} else {
			result = new While(position, simplifyCondition, simplifyInstruction, simplifyOtherwise);
		}
		if (simplifyCondition.isStatic()) {
			final ManifestBoolean cond = (ManifestBoolean) simplifyCondition.getStaticValue();
			if (cond.getValue()) {
				return new Abort(position, "Infinite loop detected");
			}
			LOGGER.debug("replace while loop by never-run");
			return simplifyOtherwise;
		}
		return result;
	}

	@Override
	public String toString() {
		return "{While " + condition + ": " + instruction + " Else " + otherwise + "}";
	}

}
