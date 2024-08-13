/*
 * © OOO "SI IKS LAB", 2022-2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cxbox.constgen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

class CodeGenerator {

	private final TypeElement typeElement;

	private final Element superclass;

	private final Elements elements;

	private final String packageName;

	private final String className;

	CodeGenerator(TypeElement typeElement, Element superclass, Elements elements) {
		this.typeElement = typeElement;
		this.superclass = superclass;
		this.elements = elements;
		this.packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
		this.className = typeElement.getSimpleName() + "_";
	}

	private static boolean isStatic(VariableElement el) {
		return el.getModifiers().contains(Modifier.STATIC);
	}

	private static boolean isStatic(final ExecutableElement el) {
		return el.getModifiers().contains(Modifier.STATIC);
	}

	JavaFile generate() {
		Builder classBuilder = TypeSpec.classBuilder(className);
		if (superclass != null) {
			classBuilder.superclass(ClassName.get(
					elements.getPackageOf(superclass).getQualifiedName().toString(),
					superclass.getSimpleName() + "_"
			));
		}
		classBuilder.addModifiers(Modifier.PUBLIC);
		for (Constant constant : collectFields()) {
			ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
					ClassName.get(DtoField.class), TypeName.get(typeElement.asType()), constant.getType()
			);
			FieldSpec fieldSpec = FieldSpec.builder(parameterizedTypeName, constant.getName())
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
					.initializer(constant.getInitializer(), constant.getName())
					.build();
			classBuilder.addField(fieldSpec);
		}
		return JavaFile.builder(packageName, classBuilder.build()).build();
	}

	private List<Constant> collectFields() {
		final List<Constant> fieldSpecs = new ArrayList<>();
		final List<VariableElement> fields = new ArrayList<>();
		final List<ExecutableElement> methods = new ArrayList<>();
		for (final Element el : typeElement.getEnclosedElements()) {
			if (el.getKind() == ElementKind.FIELD) {
				final VariableElement varEl = (VariableElement) el;
				if (!isTransient(varEl) && !isStatic(varEl)) {
					fields.add(varEl);
				}
			}
			if (el.getKind() == ElementKind.METHOD) {
				final ExecutableElement method = (ExecutableElement) el;
				if (!isStatic(method)) {
					methods.add(method);
				}
			}
		}
		fields.forEach(field -> fieldSpecs.add(
				new Constant(
						field.getSimpleName().toString(),
						TypeName.get(field.asType()).box(),
						fieldInitializer(methods, field)
				)
		));
		Collections.sort(fieldSpecs);
		return fieldSpecs;
	}

	public String fieldInitializer(final List<ExecutableElement> methods, final VariableElement field) {
		final String getterName = getterName(field);
		final boolean getterExist = hasFieldGetter(field) || hasClassGetter(typeElement) || methods.stream()
				.filter(method -> Objects.equals(getterName, method.getSimpleName().toString()))
				.map(ExecutableElement.class::cast)
				.filter(method -> method.getTypeParameters().isEmpty())
				.anyMatch(method -> Objects.equals(TypeName.get(method.getReturnType()), TypeName.get(field.asType())));
		return StringSubstitutor.replace(
				"new DtoField<>($S${getter})",
				Map.of("getter", getterExist ? ", " + methodReference(getterName) : "")
		);
	}

	private String methodReference(final String getterName) {
		return StringSubstitutor.replace(
				"${class}::${getter}",
				Map.of(
						"class", TypeName.get(typeElement.asType()),
						"getter", getterName
				)
		);
	}

	private String getterName(final Element field) {
		return StringSubstitutor.replace(
				"${prefix}${name}",
				Map.of(
						"prefix",
						(field.asType().getKind().isPrimitive() && TypeName.BOOLEAN.equals(TypeName.get(field.asType()))) ? "is"
								: "get",
						"name",
						StringUtils.capitalize(field.getSimpleName().toString())
				)
		);
	}

	private boolean isTransient(VariableElement el) {
		for (AnnotationMirror am : elements.getAllAnnotationMirrors(el)) {
			Name qualifiedName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
			if (qualifiedName.contentEquals("org.cxbox.constgen.DtoMetamodelIgnore")) {
				return true;
			}
		}
		return false;
	}

	private boolean hasFieldGetter(final VariableElement field) {
		for (final AnnotationMirror am : elements.getAllAnnotationMirrors(field)) {
			final Name qualifiedName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
			if (qualifiedName.contentEquals("lombok.Getter")) {
				return true;
			}
		}
		return false;
	}

	private boolean hasClassGetter(final TypeElement clazz) {
		for (final AnnotationMirror am : elements.getAllAnnotationMirrors(clazz)) {
			final Name qualifiedName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
			if (qualifiedName.contentEquals("lombok.Getter")) {
				return true;
			}
		}
		return false;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public String getClassName() {
		return this.className;
	}

	public static final class StringSubstitutor {

		private StringSubstitutor() {
			throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
		}

		public static String replace(String template, Map<String, Object> parameters) {
			StringBuilder newTemplate = new StringBuilder(template);
			List<Object> valueList = new ArrayList<>();

			Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);

			while (matcher.find()) {
				String key = matcher.group(1);

				String paramName = "${" + key + "}";
				int index = newTemplate.indexOf(paramName);
				if (index != -1) {
					newTemplate.replace(index, index + paramName.length(), "%s");
					valueList.add(parameters.get(key));
				}
			}

			return String.format(newTemplate.toString(), valueList.toArray());
		}

	}


	public static final class StringUtils {

		private StringUtils() {
			throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
		}

		/**
		 * Capitalizes a String changing the first character to title case as
		 * per {@link Character#toTitleCase(int)}. No other characters are changed.
		 *
		 * <pre>
		 * StringUtils.capitalize(null)  = null
		 * StringUtils.capitalize("")    = ""
		 * StringUtils.capitalize("cat") = "Cat"
		 * StringUtils.capitalize("cAt") = "CAt"
		 * StringUtils.capitalize("'cat'") = "'cat'"
		 * </pre>
		 *
		 * @param str the String to capitalize, may be null
		 * @return the capitalized String, {@code null} if null String input
		 */
		public static String capitalize(final String str) {
			final int strLen = length(str);
			if (strLen == 0) {
				return str;
			}

			final int firstCodepoint = str.codePointAt(0);
			final int newCodePoint = Character.toTitleCase(firstCodepoint);
			if (firstCodepoint == newCodePoint) {
				// already capitalized
				return str;
			}

			final int[] newCodePoints = new int[strLen]; // cannot be longer than the char array
			int outOffset = 0;
			newCodePoints[outOffset++] = newCodePoint; // copy the first code point
			for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
				final int codePoint = str.codePointAt(inOffset);
				newCodePoints[outOffset++] = codePoint; // copy the remaining ones
				inOffset += Character.charCount(codePoint);
			}
			return new String(newCodePoints, 0, outOffset);
		}

		public static int length(final CharSequence cs) {
			return cs == null ? 0 : cs.length();
		}

	}


}
