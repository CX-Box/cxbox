/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2020 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.core.service.impl;

import static org.cxbox.api.data.dao.SpecificationUtils.and;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.service.BcSpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SpecificationBuilder implements BcSpecificationBuilder {

	@Override
	public <E> Specification<E> buildBcSpecification(BusinessComponent bc, Specification<E> parentSpecification, Specification<E> specification) {
		return and(parentSpecification, specification);
	}

}
