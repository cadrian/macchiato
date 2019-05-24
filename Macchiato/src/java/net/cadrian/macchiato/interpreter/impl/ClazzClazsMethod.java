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
package net.cadrian.macchiato.interpreter.impl;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.objects.MacClazsObject;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Ruleset;

public class ClazzClazsMethod implements ClazsMethod {

	private final ClazzClazs clazzClazs;
	private final Def def;
	private final String name;
	private final Ruleset ruleset;

	ClazzClazsMethod(final ClazzClazs clazzClazs, final Def def, final String name, final Ruleset ruleset) {
		this.clazzClazs = clazzClazs;
		this.def = def;
		this.name = name;
		this.ruleset = ruleset;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Class<? extends MacObject>[] getArgTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getArgNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends MacObject> getResultType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public Clazs getTargetType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(final MacClazsObject target, final Context context, final int position) {
		// TODO Auto-generated method stub

	}

}
