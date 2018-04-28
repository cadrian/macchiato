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

public interface ExpressionVisitor
		extends CheckedExpression.Visitor, FunctionCall.Visitor, Identifier.Visitor, IndexedExpression.Visitor,
		ManifestArray.Visitor, ManifestBoolean.Visitor, ManifestDictionary.Visitor, ManifestNumeric.Visitor,
		ManifestRegex.Visitor, ManifestString.Visitor, Result.Visitor, TypedBinary.Visitor, TypedUnary.Visitor {

}
