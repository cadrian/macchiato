package net.cadrian.macchiato.recipe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import net.cadrian.macchiato.recipe.ast.Recipe;
import net.cadrian.macchiato.recipe.interpreter.Interpreter;
import net.cadrian.macchiato.recipe.parser.Parser;

public class Run {

	public static void main(final String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: " + Run.class.getSimpleName() + " <mac file> (<midi file>)");
			System.exit(1);
		}
		try (final FileReader reader = new FileReader(args[0])) {
			final Parser parser = new Parser(reader);
			final Recipe recipe = parser.parse();
			final Interpreter interpreter = new Interpreter(recipe);
			if (args.length > 1) {
				interpreter.run(new FileInputStream(args[1]), new FileOutputStream(args[1] + ".out"));
			} else {
				interpreter.run();
			}
		} catch (final IOException | InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
