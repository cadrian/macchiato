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

import net.cadrian.macchiato.ruleset.ast.AbstractCall;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class ProcedureCall extends AbstractCall implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitProcedureCall(ProcedureCall procedureCall);
	}

	public ProcedureCall(final Position position, final Expression target, final Identifier name) {
		super(position, target, name);
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitProcedureCall(this);
	}

	@Override
	public Instruction simplify() {
		final Expression simplifyTarget = target == null ? null : target.simplify();
		boolean changed = simplifyTarget != target;
		final ProcedureCall result = new ProcedureCall(position, simplifyTarget, name);
		for (final Expression arg : arguments) {
			final Expression simplifyArg = arg.simplify();
			result.add(simplifyArg);
			changed |= simplifyArg != arg;
		}
		return changed ? result : this;
	}

}
