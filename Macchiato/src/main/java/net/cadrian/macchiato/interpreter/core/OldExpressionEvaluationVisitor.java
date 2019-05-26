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
package net.cadrian.macchiato.interpreter.core;

import java.util.Map;

import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.expression.Old;

public class OldExpressionEvaluationVisitor extends ExpressionEvaluationVisitor {

	OldExpressionEvaluationVisitor(final Context context, final Map<Integer, MacObject> oldValues) {
		super(context, MacObject.class, oldValues);
	}

	@Override
	public void visitOld(final Old old) {
		lastValue = oldValues.get(old.getId());
		if (lastValue == null) {
			old.getExpression().accept(this);
			oldValues.put(old.getId(), getLastValue());
		}
	}

	public Map<Integer, MacObject> getOldValues() {
		return oldValues;
	}

}
