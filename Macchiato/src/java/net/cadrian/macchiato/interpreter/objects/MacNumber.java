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
package net.cadrian.macchiato.interpreter.objects;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.cadrian.macchiato.interpreter.Field;
import net.cadrian.macchiato.interpreter.Method;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class MacNumber implements MacComparable<MacNumber> {

	private static final Map<BigInteger, MacNumber> CACHE = new ConcurrentHashMap<>();

	public static final MacNumber ZERO = valueOf(BigInteger.ZERO);
	public static final MacNumber ONE = valueOf(BigInteger.ONE);

	private final BigInteger value;

	private MacNumber(final BigInteger value) {
		this.value = value;
	}

	public BigInteger getValue() {
		return value;
	}

	@Override
	public <T extends MacObject, R extends MacObject> Field<T, R> getField(final Ruleset ruleset, final String name) {
		return null;
	}

	@Override
	public <T extends MacObject> Method<T> getMethod(final Ruleset ruleset, final String name) {
		return null;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public int compareTo(final MacNumber other) {
		return value.compareTo(other.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	public static MacNumber valueOf(final long value) {
		return valueOf(BigInteger.valueOf(value));
	}

	public static MacNumber valueOf(final BigInteger value) {
		return CACHE.computeIfAbsent(value, MacNumber::new);
	}

	public MacNumber negate() {
		return valueOf(value.negate());
	}

	public MacNumber add(final MacNumber other) {
		return valueOf(value.add(other.value));
	}

	public MacNumber divide(final MacNumber other) {
		return valueOf(value.divide(other.value));
	}

	public MacNumber multiply(final MacNumber other) {
		return valueOf(value.multiply(other.value));
	}

	public MacNumber subtract(final MacNumber other) {
		return valueOf(value.subtract(other.value));
	}

	public MacNumber pow(final MacNumber other) {
		return valueOf(value.pow(other.value.intValueExact()));
	}

	public MacNumber remainder(final MacNumber other) {
		return valueOf(value.remainder(other.value));
	}

}
