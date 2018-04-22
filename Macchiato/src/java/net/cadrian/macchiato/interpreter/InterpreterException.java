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

public class InterpreterException extends RuntimeException {

	private static final long serialVersionUID = -695648464250538720L;

	private final int position;

	public InterpreterException(final String msg, final int position) {
		super(msg);
		this.position = position;
	}

	public InterpreterException(final String msg, final int position, final Throwable cause) {
		super(msg, cause);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

}
