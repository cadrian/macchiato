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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.ruleset.ast.Instruction;
import net.cadrian.macchiato.ruleset.ast.Node;
import net.cadrian.macchiato.ruleset.parser.Position;

public class Block implements Instruction {

	private static final Logger LOGGER = LoggerFactory.getLogger(Block.class);

	private final Position position;
	private final List<Instruction> instructions = new ArrayList<>();

	@SuppressWarnings("PMD.ImplicitFunctionalInterface")
	public interface Visitor extends Node.Visitor {
		void visitBlock(Block block);
	}

	public Block(final Position position) {
		this.position = position;
	}

	public void add(final Instruction instruction) {
		instructions.add(instruction);
	}

	@Override
	public Position position() {
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
	public Instruction simplify() {
		switch (instructions.size()) {
		case 0:
			LOGGER.debug("remove empty block");
			return DoNothing.INSTANCE;
		case 1:
			LOGGER.debug("replace block by unique instruction");
			return instructions.get(0).simplify();
		default:
			final Block result = new Block(position);
			boolean changed = false;
			for (final Instruction instruction : instructions) {
				final Instruction simplifyInstruction = instruction.simplify();
				if (simplifyInstruction.equals(DoNothing.INSTANCE)) {
					LOGGER.debug("remove instruction that does nothing");
					changed = true;
				} else {
					result.add(simplifyInstruction);
					changed |= !instruction.equals(simplifyInstruction);
				}
			}
			return changed ? result.getInstructions().isEmpty() ? DoNothing.INSTANCE : result : this;
		}
	}

	@Override
	public String toString() {
		return "{Block " + instructions + "}";
	}

}
