package net.cadrian.macchiato.interpreter;

import java.util.Arrays;

import net.cadrian.macchiato.recipe.ast.Def;
import net.cadrian.macchiato.recipe.ast.FormalArgs;

public class DefFunction implements Function {

	private final Def def;
	private final Class<?>[] argsType;
	private final String[] argNames;

	public DefFunction(final Def def) {
		this.def = def;
		final FormalArgs args = def.getArgs();
		argsType = new Class<?>[args.size()];
		Arrays.fill(argsType, Object.class);
		argNames = args.toArray();
	}

	@Override
	public String name() {
		return def.name();
	}

	@Override
	public Class<?>[] getArgTypes() {
		return argsType;
	}

	@Override
	public String[] getArgNames() {
		return argNames;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public void run(final Context context) {
		final InstructionEvaluationVisitor v = new InstructionEvaluationVisitor(context);
		def.getInstruction().accept(v);
	}

}
