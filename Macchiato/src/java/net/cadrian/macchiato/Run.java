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

import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.core.Interpreter;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.parser.Parser;

public class Run {

	private static final Logger LOGGER = LoggerFactory.getLogger(Run.class);

	public static void main(final String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: " + Run.class.getSimpleName()
					+ " <mac file> (<midi input file>) (<midi output file>) [-- <program arguments>]");
			System.exit(1);
		}

		try {
			final String rulesetName = getRulesetName(args);
			final String midiInputName = getMidiInputName(args);
			final File rulesetFile = new File(rulesetName);
			final Parser parser;
			final Ruleset ruleset;
			try (final Reader reader = new BufferedReader(new FileReader(rulesetName))) {
				parser = new Parser(rulesetFile.getParentFile(), reader, rulesetFile.getAbsolutePath());
				LOGGER.info("Parsing ruleset: {}", rulesetName);
				ruleset = parser.parse().simplify();
			}
			LOGGER.debug("Parsed ruleset: {}", ruleset);
			try {
				final Interpreter interpreter = new Interpreter(ruleset);
				if (midiInputName != null) {
					final String midiOutputName = getMidiOutputName(args);
					try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(midiInputName))) {
						try (final BufferedOutputStream out = new BufferedOutputStream(
								new FileOutputStream(midiOutputName))) {
							interpreter.run(in, out, getProgramArgs(args));
						}
					}
				} else {
					interpreter.run(getProgramArgs(args));
				}
			} catch (final InterpreterException e) {
				System.err.println(parser.error(e));
				System.exit(1);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static String getRulesetName(final String[] args) {
		return args[0];
	}

	private static String getMidiInputName(final String[] args) {
		if (args.length > 1 && !"--".equals(args[1])) {
			return args[1];
		}
		return null;
	}

	private static String getMidiOutputName(final String[] args) {
		assert getMidiInputName(args) != null;
		if (args.length > 2 && !"--".equals(args[2])) {
			return args[2];
		}
		return args[1] + ".out.mid";
	}

	private static String[] getProgramArgs(final String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("--".equals(args[i])) {
				final String[] result = new String[args.length - i - 1];
				System.arraycopy(args, i + 1, result, 0, result.length);
				return result;
			}
		}
		return new String[0];
	}

}
