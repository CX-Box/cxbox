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

package org.cxbox.core.util.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.cxbox.core.util.filter.provider.impl.LongValueProvider;
import org.cxbox.core.util.filter.provider.impl.StringValueProvider;

/**
 * Enables filtration by the annotated field of {@link org.cxbox.api.data.dto.DataResponseDTO DataResponseDTO}.
 * Configures the rules and parameters for filtering by field.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchParameter {

	/**
	 * A path to the field on a JPA entity on which filtering will be performed.
	 * For collections annotated with {@link jakarta.persistence.ElementCollection ElementCollection}
	 * provide a path to the collection field itself.
	 * For collections annotated with {@link jakarta.persistence.OneToMany OneToMany}
	 * provide a path to the association entity field.
	 *
	 * @return Dot notation path e.g. entityField.associationField.
	 */
	String name() default "";

	/**
	 * Whether to filter by datetime as-is. If not strict, datetime filtering will be done by the value
	 * at the beginning or end of the day for the LESS THAN and GREATER THAN operators, respectively.
	 * If strict, filtering will be done by the actual filter value.
	 *
	 * @return Whether to apply strict filtering or not.
	 */
	boolean strict() default false;

	/**
	 * Whether to block the mechanism at the Cxbox level or not. If blocked, filtering should be
	 * implemented at the level of services that manage the entity.
	 * By default, the Cxbox filter is not blocked.
	 *
	 * @return Whether to block the mechanism at the cxbox level or not
	 */
	boolean suppressProcess() default false;

	/**
	 * Used only for fields of {@link org.cxbox.core.dto.multivalue.MultivalueField MultivalueField} type
	 * and is necessary to correctly type string representation of filtering parameter.
	 *
	 * @return ClassifyDataProvider which could be applied to the key(id) of {@link org.cxbox.core.dto.multivalue.MultivalueFieldSingleValue MultivalueFieldSingleValue}.
	 * E.g. the key is stringified date, then we should use {@link org.cxbox.core.util.filter.provider.impl.DateValueProvider DateValueProvider}
	 */
	Class<? extends ClassifyDataProvider> multiFieldKey() default LongValueProvider.class;

	/**
	 * Get a provider for defining of classify data parameter in sorting or searching cases.
	 * Necessary to correctly type string representation of filtering parameter.
	 *
	 * @return ClassifyDataProvider
	 */
	Class<? extends ClassifyDataProvider> provider() default StringValueProvider.class;

}
