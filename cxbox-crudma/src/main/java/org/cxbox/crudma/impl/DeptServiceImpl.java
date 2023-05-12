
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

package org.cxbox.crudma.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.crudma.api.DeptService;
import org.cxbox.crudma.dto.DepartmentDTO;
import org.cxbox.crudma.meta.DeptFieldMetaBuilder;
import org.cxbox.model.core.entity.Department;
import org.springframework.stereotype.Service;

@Service
public class DeptServiceImpl extends AbstractResponseService<DepartmentDTO, Department> implements DeptService {

	public DeptServiceImpl() {
		super(DepartmentDTO.class, Department.class, null, DeptFieldMetaBuilder.class);
	}

	@Override
	public ActionResultDTO<DepartmentDTO> deleteEntity(BusinessComponent businessComponent) {
		throw new UnsupportedOperationException();
	}

}
