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
package net.cadrian.macchiato.interpreter.core.clazs;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsField;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

class ClazzClazsField implements ClazsField {

	private final ClazzClazs clazzClazs;
	private final Identifier name;
	private final Ruleset ruleset;

	ClazzClazsField(final ClazzClazs clazzClazs, final Identifier name, final Ruleset ruleset) {
		this.clazzClazs = clazzClazs;
		this.name = name;
		this.ruleset = ruleset;
	}

	@Override
	public Identifier name() {
		return name;
	}

	@Override
	public Class<MacObject> getResultType() {
		return MacObject.class;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Class<MacClazsObject> getTargetType() {
		return MacClazsObject.class;
	}

	@Override
	public Clazs getTargetClazs() {
		return clazzClazs;
	}

	@Override
	public MacObject get(MacClazsObject target, Context context, int position) {
		return target.getFieldValue(name.getName());
	}

	@Override
	public MacObject set(MacClazsObject target, Context context, int position, MacObject newValue) {
		return target.setFieldValue(name.getName(), newValue);
	}

}
