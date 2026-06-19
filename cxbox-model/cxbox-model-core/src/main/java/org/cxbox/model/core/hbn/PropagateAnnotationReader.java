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

package org.cxbox.model.core.hbn;

import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.MetadataProvider;

/**
 * Implementation of AnnotationReader adding parent entity annotations marked
 * using @PropagateAnnotations, to the annotations of the child entity. Created for the reason
 * that JPA and Hibernate annotations are not marked as @Inherited and therefore do not apply to
 * child entities
 */
public class PropagateAnnotationReader implements AnnotationReader {

	private final AnnotationReader delegate;

	private final MetadataProvider metadataProvider;

	private final Set<Class<? extends Annotation>> propagated;

	private final AnnotationReader parent;

	private final AnnotatedElement annotatedElement;

	private final Map<Class<? extends Annotation>, AnnotationPropagationGuard> guards;

	public PropagateAnnotationReader(AnnotationReader delegate,
			MetadataProvider metadataProvider, AnnotatedElement annotatedElement, Map<Class<? extends Annotation>, AnnotationPropagationGuard> guards) {
		this.delegate = delegate;
		this.metadataProvider = metadataProvider;
		propagated = getPropagatedAnnotations(annotatedElement);
		parent = getParentAnnotationReader(annotatedElement);
		this.annotatedElement = annotatedElement;
		this.guards = guards;
	}

	/**
	 * Returns a set of potentially inherited annotation types
	 *
	 * @param annotatedElement annotated element
	 * @return set of annotations
	 */
	private Set<Class<? extends Annotation>> getPropagatedAnnotations(AnnotatedElement annotatedElement) {
		if (!(annotatedElement instanceof Class)) {
			return Collections.emptySet();
		}
		PropagateAnnotations propagateAnnotations = annotatedElement.getAnnotation(PropagateAnnotations.class);
		if (propagateAnnotations == null) {
			return Collections.emptySet();
		}
		Set<Class<? extends Annotation>> result = new HashSet<>();
		Collections.addAll(result, propagateAnnotations.value());
		return result;
	}

	/**
	 * Returns an annotation of the specified type for an element, taking into account inheritance
	 *
	 * @param annotationType annotation type
	 * @return annotation
	 */
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		Annotation result = delegate.getAnnotation(annotationType);
		if (result == null && propagated.contains(annotationType) && parent != null) {
			T pAnnotation = parent.getAnnotation(annotationType);
			if (pAnnotation != null && canPropagate(annotationType)) {
				result = pAnnotation;
			}
		}
		return (T) result;
	}

	/**
	 * Returns whether an annotation of the specified type is present on the annotated element,
	 * considering inheritance via {@link PropagateAnnotations}.
	 *
	 * @param annotationType the annotation type to look up
	 * @return {@code true} if the annotation is present directly or propagated from a parent
	 */
	@Override
	public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
		boolean result = delegate.isAnnotationPresent(annotationType);
		if (!result && propagated.contains(annotationType) && parent != null) {
			result = parent.isAnnotationPresent(annotationType) && canPropagate(annotationType);
		}
		return result;
	}

	/**
	 * Returns annotations from the current element merged with inherited annotations
	 * declared in {@link PropagateAnnotations#value()}.
	 *
	 * <p>An inherited annotation is included only if it is not overridden locally and
	 * propagation is permitted by the corresponding {@link AnnotationPropagationGuard}.
	 *
	 * @return all applicable annotations, including propagated ones
	 */
	@Override
	public Annotation[] getAnnotations() {
		List<Annotation> annotations = new ArrayList<>();
		Set<Class<? extends Annotation>> propagated = new HashSet<>(this.propagated);
		for (Annotation annotation : delegate.getAnnotations()) {
			annotations.add(annotation);
			propagated.remove(annotation.annotationType());
		}
		if (parent != null) {
			for (var cls : propagated) {
				var pAnnotation = parent.getAnnotation(cls);
				if (pAnnotation != null && canPropagate(cls)) {
					annotations.add(pAnnotation);
				}
			}
		}
		return annotations.toArray(new Annotation[0]);
	}

	/**
	 * Gets the AnnotationReader of the parent element
	 *
	 * @param annotatedElement annotated element
	 * @return AnnotationReader attached to the parent element (class)
	 */
	private AnnotationReader getParentAnnotationReader(AnnotatedElement annotatedElement) {
		if (!(annotatedElement instanceof Class) || annotatedElement == Object.class) {
			return null;
		}
		return metadataProvider.getAnnotationReader(((Class) annotatedElement).getSuperclass());
	}


	/**
	 * Returns whether the given annotation type may be propagated from a parent.
	 *
	 * <p>Propagation is unconditionally allowed when no {@link AnnotationPropagationGuard}
	 * is registered. Otherwise requires {@link #annotatedElement} to be a {@link Class}
	 * and the guard to approve.
	 *
	 * @param annotationType the annotation type to check
	 * @return {@code true} if propagation is permitted
	 */
	private boolean canPropagate(@NotNull Class<? extends Annotation> annotationType) {
		AnnotationPropagationGuard guard = guards.get(annotationType);
		if (guard == null) {
			return true;
		}
		if (!(annotatedElement instanceof Class<?> cls)) {
			return false;
		}
		return guard.canPropagate(cls, metadataProvider);
	}

}
