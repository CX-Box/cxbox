/*
 * © OOO "SI IKS LAB", 2022-2026
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

import java.lang.annotation.Annotation;
import lombok.NonNull;
import org.hibernate.annotations.common.reflection.MetadataProvider;

/**
 * Guards propagation of a specific annotation type declared with {@link PropagateAnnotations#value()}.
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader} and check
 * before an annotation is propagated to a target class. If no guard is registered for
 * an annotation type, propagation is unconditionally allowed. Registration must be in {@code resources/META-INF/services/org.cxbox.model.core.hbn.AnnotationPropagationGuard}</p>
 *
 * <p></[><b>Example: </b>
 * {@link DiscriminatorOptionsPropagationGuard}
 * </p>
 * @see PropagateAnnotations
 */
public interface AnnotationPropagationGuard {


	/*
	 * @return the annotation type managed by this guard;
	 */
	Class<? extends Annotation> targetAnnotationType();


	/**
	 * Decides if the annotation can be inherited by the given target class.
	 *
	 * @param target the destination class for annotation propagation;
	 * @param provider the metadata provider
	 * @return {@code true} if propagation is permitted; {@code false} otherwise
	 */
	boolean canPropagate(@NonNull Class<?> target, @NonNull MetadataProvider provider);

}