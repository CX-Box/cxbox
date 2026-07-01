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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.boot.model.internal.JPAXMLOverriddenMetadataProvider;
import org.hibernate.boot.model.internal.XMLContext;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.BootstrapContext;

/**
 * Decorator base JPAMetadataProvider, inheritance is used because
 * HBN uses explicit conversions to JPAMetadataProvider
 */
public class PropagateMetadataProvider extends JPAXMLOverriddenMetadataProvider {

	private final JPAXMLOverriddenMetadataProvider delegate;

	private Map<AnnotatedElement, AnnotationReader> cache = new HashMap<>(100);

	private final Map<Class<? extends Annotation>, AnnotationPropagationGuard> guards;

	public PropagateMetadataProvider(BootstrapContext bootstrapContext, JPAXMLOverriddenMetadataProvider delegate) {
		super(bootstrapContext);
		this.delegate = delegate;
		this.guards = loadGuards(bootstrapContext);
	}


	@Override
	public Map<Object, Object> getDefaults() {
		return delegate.getDefaults();
	}

	@Override
	public AnnotationReader getAnnotationReader(AnnotatedElement annotatedElement) {
		// computeIfAbsent не работает в JDK9 из-за рекурсивных вызовов,
		// а использовать ConcurrentHashMap согласно документации нельзя:
		//	 * ... Some attempted update operations
		//	 * on this map by other threads may be blocked while computation
		//	 * is in progress, so the computation should be short and simple,
		//	 * and must not attempt to update any other mappings of this map.
		AnnotationReader reader = cache.get(annotatedElement);
		if (reader == null) {
			reader = new PropagateAnnotationReader(
					delegate.getAnnotationReader(annotatedElement),
					this,
					annotatedElement,
					guards
			);
			cache.put(annotatedElement, reader);
		}
		return reader;
	}

	@Override
	public XMLContext getXMLContext() {
		return delegate.getXMLContext();
	}

	/**
	 * Get registered guard like hibernate {@link org.hibernate.boot.internal.MetadataBuilderImpl#applyFunctions(FunctionContributor)}
	 */
	private Map<Class<? extends Annotation>, AnnotationPropagationGuard> loadGuards(BootstrapContext bootstrapContext) {
		var classLoaderService = bootstrapContext
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
