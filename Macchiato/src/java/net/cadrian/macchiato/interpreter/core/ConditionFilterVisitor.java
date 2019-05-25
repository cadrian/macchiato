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

import net.cadrian.macchiato.interpreter.objects.MacBoolean;
import net.cadrian.macchiato.ruleset.ast.BoundFilter;
import net.cadrian.macchiato.ruleset.ast.ConditionFilter;

class ConditionFilterVisitor implements BoundFilter.Visitor, ConditionFilter.Visitor {

	private final GlobalContext context;

	public ConditionFilterVisitor(final GlobalContext context) {
		this.context = context;
	}

	@Override
	public void visit(final ConditionFilter conditionFilter) {
		final MacBoolean condition = (MacBoolean) context.eval(conditionFilter.getCondition());
		if (condition.isTrue()) {
			context.eval(conditionFilter.getInstruction());
		}
	}

	@Override
	public void visit(final BoundFilter boundFilter) {
		// do nothing
	}

}
