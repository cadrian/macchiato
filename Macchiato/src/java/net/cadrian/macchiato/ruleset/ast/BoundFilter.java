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
package net.cadrian.macchiato.ruleset.ast;

public class BoundFilter extends Filter {

	public static enum Bound {
		BEGIN_SEQUENCE,
		END_SEQUENCE,
		BEGIN_TRACK,
		END_TRACK;
	}

	public static interface Visitor extends Node.Visitor {
		void visit(BoundFilter boundFilter);
	}

	private final int position;
	private final Bound bound;

	public BoundFilter(final int position, final Bound bound, final Instruction instruction) {
		super(instruction);
		this.position = position;
		this.bound = bound;
	}

	public Bound getBound() {
		return bound;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

	@Override
	public Filter simplify() {
		final Instruction simplifyInstruction = instruction.simplify();
		if (simplifyInstruction == instruction) {
			return this;
		}
		return new BoundFilter(position, bound, simplifyInstruction);
	}

	@Override
	public String toString() {
		return "{Filter bound:" + bound + " " + instruction + "}";
	}

}
