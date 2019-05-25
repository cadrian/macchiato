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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.ruleset.Inheritance;
import net.cadrian.macchiato.ruleset.Inheritance.Parent;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedClazz;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class ClazzClazs implements Clazs {

	private static class MethodDefinition {
		final Clazs owner;
		final Identifier name;
		final ClazsMethod method;
		final MethodDefinition superMethod;

		MethodDefinition(final Clazs owner, final Identifier name, final ClazsMethod method,
				MethodDefinition superMethod) {
			this.owner = owner;
			this.name = name;
			this.method = method;
			this.superMethod = superMethod;
		}
	}

	private final Ruleset ruleset;
	private final Identifier name;
	private final int position;
	private final Map<Identifier, MethodDefinition> methods = new HashMap<>();
	private final Set<Clazs> conformance = new HashSet<>();

	public ClazzClazs(final LocalizedClazz localizedClazz) {
		this.ruleset = localizedClazz.ruleset;
		this.position = localizedClazz.clazz.position();
		this.name = localizedClazz.clazz.name();

		final Inheritance inheritance = localizedClazz.clazz.getInheritance();
		for (final Parent parent : inheritance.getParents()) {
			resolve(parent);
		}

		for (final Def def : localizedClazz.clazz.getDefs()) {
			final Identifier name = def.name();
			final ClazzClazsMethod method = new ClazzClazsMethod(this, def, name, ruleset);
			final MethodDefinition newDefinition = new MethodDefinition(this, name, method, methods.get(name));
			methods.put(name, newDefinition);
		}
	}

	private static String dottedName(Identifier[] name, int length) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				result.append('.');
			}
			result.append(name[i].getName());
		}
		return result.toString();
	}

	private void resolve(final Parent parent) {
		final Identifier[] name = parent.getName();
		final int n = name.length - 1;
		Ruleset scope = ruleset;
		for (int i = 0; i < n; i++) {
			scope = scope.getScope(name[i]);
			if (scope == null) {
				throw new InterpreterException("Class not found (unknown scope " + name[i].getName() + " in "
						+ dottedName(name, i) + "): " + dottedName(name, name.length), position);
			}
		}
		final LocalizedClazz localizedClazz = scope.getClazz(name[n]);
		if (localizedClazz == null) {
			throw new InterpreterException("Class not found: " + dottedName(name, name.length), position);
		}
		final ClazzClazs parentClazs = new ClazzClazs(localizedClazz);
		conformance.add(parentClazs);
		for (final MethodDefinition methodDefinition : parentClazs.methods.values()) {
			final Identifier methodName = methodDefinition.name;
			methods.put(methodName, methodDefinition);
		}
	}

	@Override
	public Identifier name() {
		return name;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public ClazsMethod getMethod(final String name) {
		final MethodDefinition result = methods.get(new Identifier(name, 0));
		return result == null ? null : result.method;
	}

	@Override
	public boolean conformsTo(final Clazs clazs) {
		if (conformance.contains(clazs)) {
			return true;
		}
		for (final Clazs c : conformance) {
			if (c.conformsTo(clazs)) {
				conformance.add(clazs);
				return true;
			}
		}
		return false;
	}

}
