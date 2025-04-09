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

package org.cxbox.core.crudma.bc;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.ResponseService;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.core.service.rowmeta.RowMetaType;
import org.cxbox.model.core.entity.BaseEntity;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TestReponseService implements ResponseService {

	@Override
	public BaseEntity getOneAsEntity(BusinessComponent bc) {
		return null;
	}

	@Override
	public DataResponseDTO getOne(BusinessComponent bc) {
		return null;
	}

	@Override
	public boolean hasPersister() {
		return false;
	}

	@Override
	public ResultPage getList(BusinessComponent bc) {
		return null;
	}

	@Override
	public CreateResult createEntity(BusinessComponent bc) {
		return null;
	}

	@Override
	public ActionResultDTO updateEntity(BusinessComponent bc, DataResponseDTO data) {
		return null;
	}

	@Override
	public ActionResultDTO updateEntityNow(DataResponseDTO data) {
		return null;
	}

	@Override
	public ActionResultDTO preview(BusinessComponent bc, DataResponseDTO data) {
		return null;
	}

	@Override
	public ActionResultDTO deleteEntity(BusinessComponent bc) {
		return null;
	}

	@Override
	public ActionResultDTO invokeAction(BusinessComponent bc, String actionName, DataResponseDTO data) {
		return null;
	}

	@Override
	public ActionsDTO getAvailableActions(RowMetaType metaType, DataResponseDTO data, BusinessComponent bc) {
		return null;
	}

	@Override
	public Actions getActions() {
		return null;
	}

	@Override
	public ActionResultDTO onCancel(BusinessComponent bc) {
		return null;
	}

	@Override
	public Class<? extends FieldMetaBuilder> getFieldMetaBuilder() {
		return null;
	}

	@Override
	public long count(BusinessComponent bc) {
		return 0;
	}

	@Override
	public Class getTypeOfDTO() {
		return null;
	}

	@Override
	public Class getTypeOfEntity() {
		return null;
	}

	@Override
	public void validate(BusinessComponent bc, DataResponseDTO data) {

	}

	@Override
	public boolean isDeferredCreationSupported(BusinessComponent bc) {
		return false;
	}

	@Override
	public Object unwrap(Class cls) {
		return null;
	}

	@Override
	public AssociateResultDTO associate(List data, BusinessComponent bc) {
		return null;
	}

	@Override
	public CrudmaActionType getActionType() {
		return null;
	}

	@Override
	public BusinessComponent getBc() {
		return null;
	}

}
