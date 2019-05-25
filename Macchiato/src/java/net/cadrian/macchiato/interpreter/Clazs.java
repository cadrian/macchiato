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

import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public interface Clazs {

	Identifier name();

	Ruleset getRuleset();

	ClazsMethod getMethod(String name);

	boolean conformsTo(Clazs clazs);

}
