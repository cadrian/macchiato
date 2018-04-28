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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Interpreter;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.parser.Parser;
import net.cadrian.macchiato.ruleset.parser.ParserException;

public class Run {

	private static final Logger LOGGER = LoggerFactory.getLogger(Run.class);

	public static void main(final String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: " + Run.class.getSimpleName() + " <mac file> (<midi file>)");
			System.exit(1);
		}

		try {
			final String rulesetName = args[0];
			final File rulesetFile = new File(rulesetName);
			final Parser parser;
			final Ruleset ruleset;
			try (final Reader reader = new BufferedReader(new FileReader(rulesetName))) {
				parser = new Parser(rulesetFile.getParentFile(), reader);
				LOGGER.info("Parsing ruleset: {}", rulesetName);
				ruleset = parser.parse();
			}
			LOGGER.debug("Parsed ruleset: {}", ruleset);
			try {
				final Interpreter interpreter = new Interpreter(ruleset);
				if (args.length > 1) {
					try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(args[1]))) {
						try (final BufferedOutputStream out = new BufferedOutputStream(
								new FileOutputStream(args[1] + ".out"))) {
							interpreter.run(in, out);
						}
					}
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
