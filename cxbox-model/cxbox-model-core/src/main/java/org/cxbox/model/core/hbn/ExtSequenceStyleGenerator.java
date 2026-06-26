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

import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.annotations.Parameter;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;


public class ExtSequenceStyleGenerator extends SequenceStyleGenerator {

	public ExtSequenceStyleGenerator() {
		super();
	}

	@Override
	public void configure(GeneratorCreationContext creationContext, Properties parameters) throws MappingException {
		var mappedClass = creationContext.getPersistentClass().getMappedClass();
		Class<?> entityClass = mappedClass;
		var entityName = creationContext.getPersistentClass().getEntityName();
		while (entityClass != null && entityClass != Object.class) {
			ExtSequenceGenerator gen = entityClass.getAnnotation(ExtSequenceGenerator.class);
			if (gen != null) {
				break;
			}
			entityClass = entityClass.getSuperclass();
		}
		if (StringUtils.isNotBlank(entityName)) {
			if (entityClass != null && entityClass != Object.class) {
				parseAnnotation(entityClass, parameters);
			}
		}
		super.configure(creationContext, parameters);

	}

	@Override
	public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		final Object currentId = session.getEntityPersister(null, object)
				.getIdentifier(object, session);
		if (currentId != null) {
			return currentId;
		}
		// todo: optimize for non readonly transactions
		try (Session tempSession = session.getFactory().openSession()) {
			return super.generate(tempSession.unwrap(SharedSessionContractImplementor.class), object);
		}
	}

	private void parseAnnotation(Class<?> entityClass, Properties parameters) {
		if (entityClass != null) {
			ExtSequenceGeneratorSequenceName sequenceNameAnnotation =
					entityClass.getAnnotation(ExtSequenceGeneratorSequenceName.class);
			if (sequenceNameAnnotation != null) {
				parameters.setProperty(SEQUENCE_PARAM, sequenceNameAnnotation.value());
			}
			ExtSequenceGenerator extSequenceGeneratorAnnotation
					= entityClass.getAnnotation(ExtSequenceGenerator.class);
			if (extSequenceGeneratorAnnotation != null) {
				for (Parameter param : extSequenceGeneratorAnnotation.parameters()) {
					parameters.setProperty(param.name(), param.value());
				}
			}
		}
	}

	/**
	 * Allow manually assign for field annotated {@link ExtSequenceId}
	 * <a href="https://docs.hibernate.org/orm/6.6/javadocs/org/hibernate/id/Assigned.html">offical doc</a>
	 * <a href="https://discourse.hibernate.org/t/optimisticlockexception-when-manually-setting-the-id-for-the-entity/10975/21">discussion</a>
	 */
	@Override
	public boolean allowAssignedIdentifiers() {
		return true;
	}
}
