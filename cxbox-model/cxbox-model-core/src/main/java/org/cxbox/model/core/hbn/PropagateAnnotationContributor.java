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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.ModelsContext;
import org.hibernate.models.spi.MutableClassDetails;

/**
 * {@link AdditionalMappingContributor} that propagates annotations from parent classes
 * to their subclasses based on {@link PropagateAnnotations} declarations.
 *
 * <p>For each class annotated with {@link PropagateAnnotations}, iterates over
 * the specified annotation types and copies missing ones from the direct superclass.
 * Propagation can be conditionally suppressed via {@link AnnotationPropagationGuard} SPI.
 *
 * <p>Guards are loaded via Hibernate {@code ClassLoaderService} from
 * {@code META-INF/services/org.cxbox.model.core.hbn.AnnotationPropagationGuard}.
 */
public class PropagateAnnotationContributor implements AdditionalMappingContributor {

	/**
	 * Entry point called by Hibernate during metadata building.
	 * Iterates all registered classes and triggers annotation propagation.
	 */
	@Override
	public void contribute(
			AdditionalMappingContributions contributions,
			InFlightMetadataCollector metadata,
			ResourceStreamLocator resourceStreamLocator,
			MetadataBuildingContext buildingContext) {
		var modelsContext = buildingContext.getBootstrapContext().getModelsContext();
		var classDetailsRegistry = modelsContext.getClassDetailsRegistry();
		Set<String> processed = new HashSet<>();
		var guards = loadGuards(buildingContext);
		classDetailsRegistry.forEachClassDetails(
				cd -> processClass(cd, classDetailsRegistry, modelsContext, processed, guards)
		);
	}

	/**
	 * Recursively processes the given class and its superclass chain,
	 * propagating annotations declared in {@link PropagateAnnotations} from parent to child.
	 *
	 * @param classDetails class to process
	 * @param classDetailsRegistry registry for resolving class details
	 * @param context current models context
	 * @param guards propagation guards keyed by annotation type
	 * @param processed set of already processed class names
	 */
	private void processClass(ClassDetails classDetails, ClassDetailsRegistry classDetailsRegistry, ModelsContext context,
			Set<String> processed, Map<Class<? extends Annotation>, AnnotationPropagationGuard> guards) {
		String className = classDetails.getClassName();
		if (processed.contains(className)) {
			return;
		}
		processed.add(className);
		var propagateAnnotations = classDetails.locateAnnotationUsage(PropagateAnnotations.class, context);
		if (propagateAnnotations == null) {
			return;
		}
		var superClass = classDetails.getSuperClass();
		if (superClass == null || Object.class.getName().equals(superClass.getName())) {
			return;
		}
		processClass(superClass, classDetailsRegistry, context, processed, guards);
		if (!(classDetails instanceof MutableClassDetails mutable)) {
			return;
		}
		for (Class<? extends Annotation> annotationType : propagateAnnotations.value()) {
			if (mutable.getAnnotationUsage(annotationType, context) != null) {
				continue;
			}
			Annotation parentAnnotation = superClass.getAnnotationUsage(annotationType, context);
			if (parentAnnotation == null) {
				continue;
			}
			AnnotationPropagationGuard guard = guards.get(annotationType);
			if (guard != null && !guard.canPropagate(mutable, context)) {
				continue;
			}
			mutable.addAnnotationUsage(parentAnnotation);
		}
	}

	private static Map<Class<? extends Annotation>, AnnotationPropagationGuard> loadGuards(
			MetadataBuildingContext buildingContext) {
		var classLoaderService = buildingContext
				.getBootstrapContext()
				.getServiceRegistry()
				.requireService(ClassLoaderService.class);

		return classLoaderService
				.loadJavaServices(AnnotationPropagationGuard.class)
				.stream()
				.collect(Collectors.toMap(
						AnnotationPropagationGuard::targetAnnotationType,
						Function.identity(),
						(a, b) -> {
							throw new IllegalStateException(
									"Duplicate AnnotationPropagationGuard for: "
											+ a.targetAnnotationType().getName());
						}
				));
	}

}
