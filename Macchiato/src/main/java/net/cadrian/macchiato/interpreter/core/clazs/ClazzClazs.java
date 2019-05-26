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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsConstructor;
import net.cadrian.macchiato.interpreter.ClazsField;
import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.core.Context;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Expression;
import net.cadrian.macchiato.ruleset.ast.Inheritance;
import net.cadrian.macchiato.ruleset.ast.Inheritance.Parent;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedClazz;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;

public class ClazzClazs implements Clazs {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClazzClazs.class);

	@FunctionalInterface
	public static interface ClassRepository {
		Clazs getClazs(LocalizedClazz localizedClazz);
	}

	private static class MethodDefinition {
		@SuppressWarnings("unused")
		final Clazs owner;
		final Identifier name;
		final ClazsMethod method;
		final List<MethodDefinition> precursorMethods = new ArrayList<>();

		MethodDefinition(final Clazs owner, final Identifier name, final ClazsMethod method) {
			this.owner = owner;
			this.name = name;
			this.method = method;
		}

		void addPrecursorMethod(final MethodDefinition precursorMethod) {
			precursorMethods.add(precursorMethod);
		}

		@Override
		public String toString() {
			return "{MethodDefinition name=" + name + " method=" + method + "}";
		}
	}

	private static class FieldDefinition {
		@SuppressWarnings("unused")
		final Clazs owner;
		final Identifier name;
		final ClazsField field;

		FieldDefinition(final Clazs owner, final Identifier name, final ClazsField field) {
			this.owner = owner;
			this.name = name;
			this.field = field;
		}

		@Override
		public String toString() {
			return "{FieldDefinition name=" + name + " field=" + field + "}";
		}
	}

	private final Ruleset ruleset;
	private final Identifier name;
	private final Expression invariant;
	private final Map<Identifier, MethodDefinition> methods = new HashMap<>();
	private final Map<Identifier, FieldDefinition> fields = new HashMap<>();
	private final ClazsConstructor constructor;

	private final Set<ClazzClazs> parents = new HashSet<>();
	private final Set<Clazs> conformanceCache = new HashSet<>();

	public ClazzClazs(final ClassRepository repository, final LocalizedClazz localizedClazz) {
		this.ruleset = localizedClazz.ruleset;
		this.name = localizedClazz.clazz.name();
		this.invariant = localizedClazz.clazz.getInvariant();

		final Map<Identifier, Map<Clazs, MethodDefinition>> precursors = new HashMap<>();
		final Inheritance inheritance = localizedClazz.clazz.getInheritance();
		if (inheritance != null) {
			for (final Parent parent : inheritance.getParents()) {
				resolve(repository, parent, precursors);
			}
			LOGGER.debug("resolved precursors for {}: {}", name, precursors);
		}

		for (final Identifier field : localizedClazz.clazz.getFields()) {
			final FieldDefinition newDefinition = new FieldDefinition(this, field,
					new ClazzClazsField(this, field, ruleset));
			final FieldDefinition parentDefinition = fields.put(field, newDefinition);
			if (parentDefinition != null) {
				throw new InterpreterException("Field already exists: " + field.getName(),
						parentDefinition.name.position(), field.position());
			}
		}

		ClazsConstructor constructor = null;
		for (final Def def : localizedClazz.clazz.getDefs()) {
			final Identifier name = def.name();
			if (name.equals(this.name)) {
				constructor = new ClazzClazsConstructor(this, def, name, ruleset);
			} else {
				final ClazzClazsMethod method = new ClazzClazsMethod(this, def, name, ruleset);
				final MethodDefinition newDefinition = new MethodDefinition(this, name, method);
				final Map<Clazs, MethodDefinition> precursorDefinitions = precursors.remove(name);
				if (precursorDefinitions != null) {
					for (final MethodDefinition precursorDefinition : precursorDefinitions.values()) {
						final Class<? extends MacObject>[] precursorArgTypes = precursorDefinition.method.getArgTypes();
						final Class<? extends MacObject>[] newArgTypes = method.getArgTypes();

						if (precursorArgTypes.length != newArgTypes.length) {
							throw new InterpreterException("Invalid redefinition: not the same number of arguments",
									precursorDefinition.name.position(), name.position());
						}

						newDefinition.addPrecursorMethod(precursorDefinition);

						final MethodDefinition precursor = ((ClazzClazs) precursorDefinition.owner).methods.get(name);
						method.addPrecursor((ClazzClazsMethod) precursor.method);
					}
				}
				methods.put(name, newDefinition);
			}
		}
		for (final Map.Entry<Identifier, Map<Clazs, MethodDefinition>> precursorsEntry : precursors.entrySet()) {
			final Identifier methodName = precursorsEntry.getKey();
			final Iterator<MethodDefinition> precursorMethodsIterator = precursorsEntry.getValue().values().iterator();
			MethodDefinition concretePrecursorMethod = null;
			while (precursorMethodsIterator.hasNext()) {
				final MethodDefinition precursorMethod = precursorMethodsIterator.next();
				LOGGER.debug("Checking method: {}", precursorMethod);
				if (precursorMethod.method.isConcrete()) {
					if (concretePrecursorMethod == null) {
						concretePrecursorMethod = precursorMethod;
					} else {
						throw new InterpreterException(
								"Duplicate parent definition of " + methodName.getName() + " -- need redefine",
								precursorMethod.name.position(), concretePrecursorMethod.name.position());
					}
				}
			}
			if (concretePrecursorMethod != null) {
				methods.put(methodName, concretePrecursorMethod);
			} else {
				// not concrete, take whichever one, it's OK
				methods.put(methodName, precursorsEntry.getValue().values().iterator().next());
			}
		}
		this.constructor = constructor == null ? new ClazzClazsDefaultConstructor(this, name, ruleset) : constructor;
	}

	private static String dottedName(final Identifier[] name, final int length) {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				result.append('.');
			}
			result.append(name[i].getName());
		}
		return result.toString();
	}

	private void resolve(final ClassRepository repository, final Parent parent,
			final Map<Identifier, Map<Clazs, MethodDefinition>> precursors) {
		final Identifier[] name = parent.getName();
		final int n = name.length - 1;
		Ruleset scope = ruleset;
		for (int i = 0; i < n; i++) {
			scope = scope.getScope(name[i]);
			if (scope == null) {
				throw new InterpreterException("Class not found (unknown scope " + name[i].getName() + " in "
						+ dottedName(name, i) + "): " + dottedName(name, name.length), parent.position());
			}
		}
		final LocalizedClazz localizedClazz = scope.getClazz(name[n]);
		if (localizedClazz == null) {
			throw new InterpreterException("Class not found: " + dottedName(name, name.length), parent.position());
		}
		final ClazzClazs parentClazs = (ClazzClazs) repository.getClazs(localizedClazz);
		parents.add(parentClazs);
		conformanceCache.add(parentClazs);

		for (final FieldDefinition fieldDefinition : parentClazs.fields.values()) {
			final Identifier fieldName = fieldDefinition.name;
			final FieldDefinition otherDefinition = fields.put(fieldName, fieldDefinition);
			if (otherDefinition != null) {
				throw new InterpreterException("Field conflict: " + fieldName.getName(),
						otherDefinition.name.position(), fieldName.position());
			}
		}

		for (final MethodDefinition methodDefinition : parentClazs.methods.values()) {
			final Identifier methodName = methodDefinition.name;
			final Map<Clazs, MethodDefinition> newPrecursorMethods = new LinkedHashMap<>();
			final Map<Clazs, MethodDefinition> methods = precursors.putIfAbsent(methodName, newPrecursorMethods);
			if (methods == null) {
				newPrecursorMethods.put(parentClazs, methodDefinition);
			} else {
				final MethodDefinition precursorDefinition = methods.values().iterator().next();
				final Class<? extends MacObject>[] precursorArgTypes = precursorDefinition.method.getArgTypes();
				final Class<? extends MacObject>[] methodArgTypes = methodDefinition.method.getArgTypes();

				if (precursorArgTypes.length != methodArgTypes.length) {
					throw new InterpreterException("Invalid redefinition: not the same number of arguments",
							precursorDefinition.name.position(), methodName.position(), parent.position());
				}

				methods.put(parentClazs, methodDefinition);
			}
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
	public ClazsConstructor getConstructor() {
		return constructor;
	}

	@Override
	public ClazsField getField(final Ruleset ruleset, final Identifier name) {
		final FieldDefinition result = fields.get(name);
		return result == null ? null : result.field;
	}

	@Override
	public ClazsMethod getMethod(final Ruleset ruleset, final Identifier name) {
		final MethodDefinition result = methods.get(name);
		return result == null ? null : result.method;
	}

	@Override
	public boolean conformsTo(final Clazs clazs) {
		if (conformanceCache.contains(clazs)) {
			return true;
		}
		for (final Clazs c : conformanceCache) {
			if (c.conformsTo(clazs)) {
				conformanceCache.add(clazs);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "{ClazzClazs name=" + name + " fields=" + fields + " methods=" + methods + "}";
	}

	void checkInvariant(final Context context) {
		context.checkContract(invariant, name.getName() + " invariant");
		for (final ClazzClazs parent : parents) {
			parent.checkInvariant(context);
		}
	}

}
