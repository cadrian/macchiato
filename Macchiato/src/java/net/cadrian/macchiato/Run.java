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
package net.cadrian.macchiato;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

import net.cadrian.macchiato.interpreter.Interpreter;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.parser.Parser;
import net.cadrian.macchiato.ruleset.parser.ParserException;

public class Run {

	public static void main(final String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: " + Run.class.getSimpleName() + " <mac file> (<midi file>)");
			System.exit(1);
		}
		try (final FileReader reader = new FileReader(args[0])) {
			final Parser parser = new Parser(reader);
			try {
				final Ruleset ruleset = parser.parse();
				final Interpreter interpreter = new Interpreter(ruleset);
				if (args.length > 1) {
					interpreter.run(new FileInputStream(args[1]), new FileOutputStream(args[1] + ".out"));
				} else {
					interpreter.run();
				}
			} catch (final InterpreterException e) {
				throw new ParserException(parser.error(e.getMessage(), e.getPosition()), e);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
