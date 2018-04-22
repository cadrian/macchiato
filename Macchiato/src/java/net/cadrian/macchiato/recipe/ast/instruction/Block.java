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
package net.cadrian.macchiato.recipe.ast.instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.cadrian.macchiato.recipe.ast.Instruction;
import net.cadrian.macchiato.recipe.ast.Node;

public class Block implements Instruction {

	public static interface Visitor extends Node.Visitor {
		void visitBlock(Block block);
	}

	private final int position;
	private final List<Instruction> instructions = new ArrayList<>();

	public Block(final int position) {
		this.position = position;
	}

	public void add(final Instruction instruction) {
		instructions.add(instruction);
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitBlock(this);
	}

	public List<Instruction> getInstructions() {
		return Collections.unmodifiableList(instructions);
	}

	@Override
	public String toString() {
		return "{Block " + instructions + "}";
	}

}
