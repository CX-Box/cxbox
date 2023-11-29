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

package org.cxbox.meta.crudma.meta;


import static org.cxbox.meta.data.SystemSettingDTO_.key;
import static org.cxbox.meta.data.SystemSettingDTO_.value;

import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.meta.data.SystemSettingDTO;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.meta.entity.SystemSetting;
import org.cxbox.model.core.dao.JpaDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemSettingFieldMetaBuilder extends FieldMetaBuilder<SystemSettingDTO> {

	private final JpaDao jpaDao;

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<SystemSettingDTO> fields, InnerBcDescription bcDescription,
			Long rowId, Long parRowId) {
		fields.setEnabled(
				key,
				value
		);
		if (rowId != null) {
			SystemSetting systemSetting = jpaDao.findById(SystemSetting.class, rowId);
			if (systemSetting != null && systemSetting.getKey() != null) {
				fields.setDisabled(key);
			}
		}
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<SystemSettingDTO> fields, InnerBcDescription bcDescription,
			Long parRowId) {
		fields.enableFilter(
				value,
				key
		);
	}

}
