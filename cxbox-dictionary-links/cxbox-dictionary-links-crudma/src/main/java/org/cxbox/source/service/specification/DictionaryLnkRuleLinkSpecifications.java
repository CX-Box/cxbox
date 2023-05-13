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

package org.cxbox.source.service.specification;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.service.spec.LinkSpecificationHolder;
import org.cxbox.core.service.spec.SpecificationHeader;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService_;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule_;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

@Service
public class DictionaryLnkRuleLinkSpecifications extends LinkSpecificationHolder<DictionaryLnkRule> {

	public DictionaryLnkRuleLinkSpecifications() {
		specificationHeader = SpecificationName.class;
		map = ImmutableMap.<SpecificationHeader<DictionaryLnkRule>, ParentSpecification<DictionaryLnkRule>>builder()
				.put(SpecificationName.LINK_SS_1, (bcDescription, parentId) -> (root, cq, cb) ->
						cb.equal(
								root.get(DictionaryLnkRule_.service).get(CustomizableResponseService_.id),
								NumberUtils.createLong(parentId)
						)
				).build();
	}

	public enum SpecificationName implements SpecificationHeader<DictionaryLnkRule> {

		LINK_SS_1;

		@Override
		public LOV toLOV() {
			return new LOV(this.name());
		}

	}

}
