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

import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class Local implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitLocal(Local local);
	}

	private final int position;
	private final Identifier local;
	private final Expression initializer;

	public Local(final int position, final Identifier local, final Expression initializer) {
		this.position = position;
		this.local = local;
		this.initializer = initializer;
	}

	public Identifier getLocal() {
		return local;
	}

	public Expression getInitializer() {
		return initializer;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitLocal(this);
	}

	@Override
	public Instruction simplify() {
		final Identifier simplifyLocal = local.simplify();
		final Expression simplifyInitializer = initializer.simplify();
		if (simplifyLocal == local && simplifyInitializer == initializer) {
			return this;
		}
		return new Local(position, simplifyLocal, simplifyInitializer);
	}

	@Override
	public String toString() {
		return "{Local " + local + (initializer == null ? "" : " = " + initializer) + "}";
	}

}
