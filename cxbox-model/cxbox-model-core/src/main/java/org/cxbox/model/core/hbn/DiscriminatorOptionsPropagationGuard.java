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

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Inheritance;
import java.lang.annotation.Annotation;
import lombok.NonNull;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.models.spi.ModelsContext;
import org.hibernate.models.spi.MutableClassDetails;

/**
 * {@link AnnotationPropagationGuard} for {@link DiscriminatorOptions}.
 *
 * <p>Propagation is allowed only if the target class declares
 * {@link DiscriminatorColumn} or {@link Inheritance}.
 * Register in: {@code resources/META-INF/services/org.cxbox.model.core.hbn.AnnotationPropagationGuard}
 */
public class DiscriminatorOptionsPropagationGuard implements AnnotationPropagationGuard {

	@Override
	public Class<? extends Annotation> targetAnnotationType() {
		return DiscriminatorOptions.class;
	}

	/**
	 * @return {@code true} if target declares {@link DiscriminatorColumn} or {@link Inheritance}
	 */
	@Override
	public boolean canPropagate(@NonNull MutableClassDetails target, @NonNull ModelsContext context) {
		return target.getAnnotationUsage(DiscriminatorColumn.class, context) != null
				|| target.getAnnotationUsage(Inheritance.class, context) != null;
	}

}