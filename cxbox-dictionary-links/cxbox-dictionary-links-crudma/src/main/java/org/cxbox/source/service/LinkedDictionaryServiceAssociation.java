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

package org.cxbox.source.service;

import org.cxbox.core.crudma.bc.BcIdentifier;
import org.cxbox.core.crudma.bc.EnumBcIdentifier;
import org.cxbox.core.crudma.bc.impl.AbstractEnumBcSupplier;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.source.service.data.CustomizableResponseSrvsService;
import org.cxbox.source.service.data.DictionaryLnkRuleCondService;
import org.cxbox.source.service.data.DictionaryLnkRuleService;
import org.cxbox.source.service.data.DictionaryLnkRuleValueAssocService;
import org.cxbox.source.service.data.DictionaryLnkRuleValueService;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.stereotype.Component;

@Getter
public enum LinkedDictionaryServiceAssociation implements EnumBcIdentifier {

	// @formatter:off

	customizableResponseService(CustomizableResponseSrvsService.class),
		lovLnkRule(customizableResponseService, DictionaryLnkRuleService.class),
			lovLnkRuleCond(lovLnkRule, DictionaryLnkRuleCondService.class),
			lovLnkRuleValue(lovLnkRule, DictionaryLnkRuleValueService.class),
			lovLnkRuleValueAssoc(lovLnkRule, DictionaryLnkRuleValueAssocService.class)
	;
	// @formatter:on

	public static final Holder<LinkedDictionaryServiceAssociation> Holder = new Holder<>(
			LinkedDictionaryServiceAssociation.class);

	private final BcDescription bcDescription;

	LinkedDictionaryServiceAssociation(String parentName, Class<?> serviceClass, boolean refresh) {
		this.bcDescription = buildDescription(parentName, serviceClass, refresh);
	}

	LinkedDictionaryServiceAssociation(String parentName, Class<?> serviceClass) {
		this(parentName, serviceClass, false);
	}

	LinkedDictionaryServiceAssociation(BcIdentifier parent, Class<?> serviceClass, boolean refresh) {
		this(parent == null ? null : parent.getName(), serviceClass, refresh);
	}

	LinkedDictionaryServiceAssociation(BcIdentifier parent, Class<?> serviceClass) {
		this(parent, serviceClass, false);
	}

	LinkedDictionaryServiceAssociation(Class<?> serviceClass, boolean refresh) {
		this((String) null, serviceClass, refresh);
	}

	LinkedDictionaryServiceAssociation(Class<?> serviceClass) {
		this((String) null, serviceClass, false);
	}

	@Override
	public String getName() {
		return bcDescription.getName();
	}

	@Override
	public String getParentName() {
		return bcDescription.getParentName();
	}

	public boolean isBc(BcIdentifier other) {
		if (other == null) {
			return false;
		}
		return new EqualsBuilder()
				.append(getName(), other.getName())
				.append(getParentName(), other.getParentName())
				.isEquals();
	}

	public boolean isNotBc(BcIdentifier other) {
		return !isBc(other);
	}

	@Component
	public static class LinkedDictionaryBcSupplier extends AbstractEnumBcSupplier<LinkedDictionaryServiceAssociation> {

		public LinkedDictionaryBcSupplier() {
			super(LinkedDictionaryServiceAssociation.Holder);
		}

	}

}
