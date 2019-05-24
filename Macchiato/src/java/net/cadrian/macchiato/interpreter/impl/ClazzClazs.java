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
import net.cadrian.macchiato.ruleset.Inheritance.Parent.Adapt;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedClazz;

public class ClazzClazs implements Clazs {

	private static class MethodDefinition {
		final Clazs owner;
		final String name;
		final ClazsMethod method;
		final boolean conformant;

		MethodDefinition(final Clazs owner, final String name, final ClazsMethod method, final boolean conformant) {
			this.owner = owner;
			this.name = name;
			this.method = method;
			this.conformant = conformant;
		}
	}

	private final Ruleset ruleset;
	private final String name;
	private final int position;
	private final Map<String, MethodDefinition> methods = new HashMap<>();
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
			final String name = def.name();
			final MethodDefinition methodDefinition = methods.get(name);
			final MethodDefinition newDefinition;
			final ClazzClazsMethod method = new ClazzClazsMethod(this, def, name, ruleset);
			if (methodDefinition == null) {
				// new method
				newDefinition = new MethodDefinition(this, name, method, true);
			} else {
				final boolean conformant = methodDefinition.conformant && conformance.contains(methodDefinition.owner);
				newDefinition = new MethodDefinition(this, name, method, conformant);
			}
			methods.put(name, newDefinition);
		}
	}

	private void resolve(final Parent parent) {
		final String[] name = parent.getName();
		final int n = name.length - 1;
		Ruleset scope = ruleset;
		for (int i = 0; i < n; i++) {
			scope = scope.getScope(name[i]);
		}
		final LocalizedClazz localizedClazz = scope.getClazz(name[n]);
		if (localizedClazz == null) {
			throw new InterpreterException("Class not found: " + String.join(".", name), position);
		}
		final ClazzClazs parentClazs = new ClazzClazs(localizedClazz);
		final boolean conformant = !parent.isPrivate();
		if (conformant) {
			conformance.add(parentClazs);
		}
		final Adapt adapt = parent.getAdapt();
		for (final MethodDefinition methodDefinition : parentClazs.methods.values()) {
			final String adaptedMethodName = adapt == null ? methodDefinition.name
					: adapt.getRename(methodDefinition.name);
			MethodDefinition adaptedMethod = methods.get(adaptedMethodName);
			if (adaptedMethod == null) {
				adaptedMethod = methodDefinition;
			} else if (adaptedMethod.conformant && !conformant) {
				// Ignored (lost)
			} else if (adaptedMethod.conformant != conformant || !adaptedMethodName.equals(methodDefinition.name)) {
				adaptedMethod = new MethodDefinition(parentClazs, adaptedMethodName, methodDefinition.method,
						conformant && adaptedMethod.conformant);
			} else {
				// Keep the original conformant method
			}
			methods.put(adaptedMethodName, adaptedMethod);
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Ruleset getRuleset() {
		return ruleset;
	}

	@Override
	public ClazsMethod getMethod(final String name) {
		final MethodDefinition result = methods.get(name);
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
