package net.cadrian.macchiato.interpreter;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

class RandomFunction implements Function {

	private static final SecureRandom RANDOM;
	static {
		SecureRandom random;
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (final NoSuchAlgorithmException e) {
			random = new SecureRandom();
		}
		RANDOM = random;
	}

	private static final Class<?>[] ARG_TYPES = { BigInteger.class };
	private static final String[] ARG_NAMES = { "max" };

	@Override
	public String name() {
		return "random";
	}

	@Override
	public Class<?>[] getArgTypes() {
		return ARG_TYPES;
	}

	@Override
	public String[] getArgNames() {
		return ARG_NAMES;
	}

	@Override
	public Class<?> getResultType() {
		return BigInteger.class;
	}

	@Override
	public void run(final Context context, final int position) {
		final BigInteger max = context.get("max");
		if (max.signum() != 1) {
			throw new InterpreterException("invalid max value: must be strictly positive", position);
		}
		final int resultSize = RANDOM.nextInt((max.bitLength() + 7) / 8);
		final byte[] resultBytes = new byte[resultSize];
		RANDOM.nextBytes(resultBytes);
		final BigInteger result = new BigInteger(1, resultBytes);
		context.set("result", result.mod(max));
	}

}
