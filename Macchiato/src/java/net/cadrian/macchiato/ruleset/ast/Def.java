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

import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class Def implements Node {

	public static interface Visitor extends Node.Visitor {
		void visit(Def def);
	}

	private final Identifier name;
	private final Clazz clazz;
	private final FormalArgs args;
	private final Instruction instruction;
	private final int position;
	private final Expression requires;
	private final Expression ensures;

	public Def(final int position, final Identifier name, final FormalArgs args, final Expression requires,
			final Expression ensures, final Instruction instruction, final Clazz clazz) {
		assert instruction != null || clazz != null;
		this.position = position;
		this.name = name;
		this.requires = requires;
		this.ensures = ensures;
		this.clazz = clazz;
		this.args = args;
		this.instruction = instruction;
	}

	public Identifier name() {
		return name;
	}

	public FormalArgs getArgs() {
		return args;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public Expression getRequires() {
		return requires;
	}

	public Expression getEnsures() {
		return ensures;
	}

	public Clazz getClazz() {
		return clazz;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visit(this);
	}

	public Def simplify() {
		final Instruction simplifyInstruction = instruction.simplify();
		final Clazz simplifyClazz = clazz == null ? null : clazz.simplify();
		final Expression simplifyRequires = requires == null ? null : requires.simplify();
		final Expression simplifyEnsures = ensures == null ? null : ensures.simplify();
		if (simplifyInstruction == instruction && simplifyRequires == requires && simplifyEnsures == ensures
				&& simplifyClazz == clazz) {
			return this;
		}
		return new Def(position, name, args, simplifyRequires, simplifyEnsures, simplifyInstruction, simplifyClazz);
	}

	@Override
	public String toString() {
		return "{Def: " + name + args + ": " + instruction + "}";
	}

}
