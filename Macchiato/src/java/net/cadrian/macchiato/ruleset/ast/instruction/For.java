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

public class For implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitFor(For f);
	}

	private final int position;
	private final Expression name1;
	private final Expression name2;
	private final Expression loop;
	private final Instruction instruction;

	public For(final int position, final Expression name1, final Expression name2, final Expression loop,
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
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitFor(this);
	}

	@Override
	public String toString() {
		return "{For " + name1 + (name2 == null ? "" : ", " + name2) + " in " + loop + " do " + instruction + "}";
	}

}
