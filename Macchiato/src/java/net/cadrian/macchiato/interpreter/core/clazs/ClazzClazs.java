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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.cadrian.macchiato.interpreter.Clazs;
import net.cadrian.macchiato.interpreter.ClazsConstructor;
import net.cadrian.macchiato.interpreter.ClazsField;
import net.cadrian.macchiato.interpreter.ClazsMethod;
import net.cadrian.macchiato.interpreter.InterpreterException;
import net.cadrian.macchiato.interpreter.objects.MacObject;
import net.cadrian.macchiato.ruleset.ast.Def;
import net.cadrian.macchiato.ruleset.ast.Inheritance;
import net.cadrian.macchiato.ruleset.ast.Inheritance.Parent;
import net.cadrian.macchiato.ruleset.ast.Ruleset;
import net.cadrian.macchiato.ruleset.ast.Ruleset.LocalizedClazz;
import net.cadrian.macchiato.ruleset.ast.expression.Identifier;
import net.cadrian.macchiato.ruleset.parser.Position;

public class ClazzClazs implements Clazs {

	private static class MethodDefinition {
		@SuppressWarnings("unused")
		final Clazs owner;
		final Identifier name;
		final ClazsMethod method;
		@SuppressWarnings("unused")
		final MethodDefinition precursorMethod;

		MethodDefinition(final Clazs owner, final Identifier name, final ClazsMethod method,
				final MethodDefinition precursorMethod) {
			this.owner = owner;
			this.name = name;
			this.method = method;
			this.precursorMethod = precursorMethod;
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
	}

	private final Ruleset ruleset;
	private final Identifier name;
	private final Position position;
	private final Map<Identifier, MethodDefinition> methods = new HashMap<>();
	private final Map<Identifier, FieldDefinition> fields = new HashMap<>();
	private final ClazsConstructor constructor;

	private final Set<Clazs> conformance = new HashSet<>();

	public ClazzClazs(final LocalizedClazz localizedClazz) {
		this.ruleset = localizedClazz.ruleset;
		this.position = localizedClazz.clazz.position();
		this.name = localizedClazz.clazz.name();

		final Inheritance inheritance = localizedClazz.clazz.getInheritance();
		for (final Parent parent : inheritance.getParents()) {
			resolve(parent);
		}

		for (final Identifier field : localizedClazz.clazz.getFields()) {
			final FieldDefinition newDefinition = new FieldDefinition(this, field,
					new ClazzClazsField(this, field, ruleset));
			final FieldDefinition parentDefinition = fields.put(field, newDefinition);
			if (parentDefinition != null) {
				throw new InterpreterException("Field conflict: " + field.getName(), parentDefinition.name.position(),
						field.position());
			}
		}

		ClazsConstructor constructor = null;
		for (final Def def : localizedClazz.clazz.getDefs()) {
			final Identifier name = def.name();
			if (name.equals(this.name)) {
				constructor = new ClazzClazsConstructor(this, def, name, ruleset);
			} else {
				final ClazzClazsMethod method = new ClazzClazsMethod(this, def, name, ruleset);
				final MethodDefinition newDefinition = new MethodDefinition(this, name, method, methods.get(name));

				final MethodDefinition oldDefinition = methods.put(name, newDefinition);
				if (oldDefinition != null) {
					final Class<? extends MacObject>[] oldArgTypes = oldDefinition.method.getArgTypes();
					final Class<? extends MacObject>[] newArgTypes = method.getArgTypes();

					if (oldArgTypes.length != newArgTypes.length) {
						throw new InterpreterException("Invalid redefinition: not the same number of arguments",
								oldDefinition.name.position(), name.position());
					}

					// TODO should we check if the types are compatible?
				}
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
			final MethodDefinition otherDefinition = methods.put(methodName, methodDefinition);
			if (otherDefinition != null) {
				throw new InterpreterException("Duplicate parent definition of " + methodName.getName(),
						otherDefinition.name.position(), methodName.position());
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
