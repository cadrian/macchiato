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
package net.cadrian.macchiato.ruleset.ast.expression;

import java.math.BigInteger;

import net.cadrian.macchiato.interpreter.objects.MacNumber;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Node;

public class ManifestNumeric implements ManifestExpression<BigInteger> {

	public static interface Visitor extends Node.Visitor {
		void visitManifestNumeric(ManifestNumeric manifestNumeric);
	}

	private final BigInteger value;
	private final int position;

	public ManifestNumeric(final int position, final BigInteger value) {
		this.value = value;
		this.position = position;
	}

	@Override
	public TypedExpression typed(final Class<? extends MacObject> type) {
		if (type.isAssignableFrom(MacNumber.class)) {
			return this;
		}
		return null;
	}

	@Override
	public Class<? extends MacObject> getType() {
		return MacNumber.class;
	}

	@Override
	public BigInteger getValue() {
		return value;
	}

	@Override
	public int position() {
		return position;
	}

	@Override
	public void accept(final Node.Visitor v) {
		((Visitor) v).visitManifestNumeric(this);
	}

	@Override
	public TypedExpression simplify() {
		return this;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public Expression getStaticValue() {
		return this;
	}

	@Override
	public String toString() {
		return "{ManifestNumeric " + value + "}";
	}

}
