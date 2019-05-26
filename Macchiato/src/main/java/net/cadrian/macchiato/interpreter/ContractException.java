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
package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.ruleset.parser.Position;

public class ContractException extends InterpreterException {

	private static final long serialVersionUID = 482106642864401027L;

	public ContractException(final String msg, final Position... position) {
		super(msg, position);
	}

	public ContractException(final String msg, final Throwable cause, final Position... position) {
		super(msg, cause, position);
	}

	public ContractException(final String msg, final InterpreterException e) {
		super(msg, e);
	}

}
