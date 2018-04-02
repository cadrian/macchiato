package net.cadrian.macchiato.recipe;

import java.io.FileReader;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import net.cadrian.macchiato.recipe.ast.Recipe;
import net.cadrian.macchiato.recipe.interpreter.Interpreter;
import net.cadrian.macchiato.recipe.parser.Parser;

public class Run {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: " + Run.class.getSimpleName() + " <mac file>");
			System.exit(1);
		}
		try (final FileReader reader = new FileReader(args[0])) {
			final Parser parser = new Parser(reader);
			final Recipe recipe = parser.parse();
			new Interpreter(recipe).run();
		} catch (final IOException | InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
