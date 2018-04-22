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
package net.cadrian.macchiato.recipe.ast;

import net.cadrian.macchiato.recipe.ast.instruction.Block;

public class Def implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Def def);
	}

	private final String name;
	private final FormalArgs args;
	private final Block block;
	private final int position;

	public Def(final int position, final String name, final FormalArgs args, final Block block) {
		this.position = position;
		this.name = name;
		this.args = args;
		this.block = block;
	}

	public String name() {
		return name;
	}

	public FormalArgs getArgs() {
		return args;
	}

	public Block getBlock() {
		return block;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

}
