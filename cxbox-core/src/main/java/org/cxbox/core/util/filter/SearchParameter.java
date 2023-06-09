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

import org.cxbox.core.util.filter.provider.impl.LongValueProvider;
import org.cxbox.core.util.filter.provider.impl.StringValueProvider;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchParameter {

	String name() default "";

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
	 * In case of multi-field value use as the key
	 * @return ClassifyDataProvider
	 */
	Class<? extends ClassifyDataProvider> multiFieldKey() default LongValueProvider.class;

	/**
	 * Get a provider for defining of classify data parameter in sorting or searching cases
	 * @return ClassifyDataProvider
	 */
	Class<? extends ClassifyDataProvider> provider() default StringValueProvider.class;

}
