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
package net.cadrian.macchiato.interpreter;

import net.cadrian.macchiato.ruleset.ast.BoundFilter;
import net.cadrian.macchiato.ruleset.ast.BoundFilter.Bound;
import net.cadrian.macchiato.ruleset.ast.ConditionFilter;

class BoundFilterVisitor implements BoundFilter.Visitor, ConditionFilter.Visitor {

	private final GlobalContext context;
	private final Bound bound;

	public BoundFilterVisitor(final GlobalContext context, final Bound bound) {
		this.context = context;
		this.bound = bound;
	}

	@Override
	public void visit(final ConditionFilter conditionFilter) {
		// do nothing
	}

	@Override
	public void visit(final BoundFilter boundFilter) {
		if (boundFilter.getBound() == bound) {
			context.eval(boundFilter.getInstructions());
		}
	}

}
