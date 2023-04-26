/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
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

package org.cxbox.core.crudma.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.api.data.dto.rowmeta.FieldsDTO;
import org.cxbox.api.data.dto.rowmeta.PreviewResult;
import org.cxbox.core.crudma.Crudma;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.RowMetaDTO;
import java.util.List;
import java.util.Map;


public abstract class AbstractCrudmaService implements Crudma {


	@Override
	public DataResponseDTO get(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public ResultPage<? extends DataResponseDTO> getAll(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public CreateResult create(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public PreviewResult preview(BusinessComponent bc, Map<String, Object> data) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public ActionResultDTO update(BusinessComponent bc, Map<String, Object> data) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public ActionResultDTO delete(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public ActionResultDTO invokeAction(BusinessComponent bc,
			String actionName,
			Map<String, Object> data) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public AssociateResultDTO associate(BusinessComponent bc, List<AssociateDTO> data) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public MetaDTO getMetaNew(BusinessComponent bc, CreateResult data) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public MetaDTO getMeta(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public MetaDTO getOnFieldUpdateMeta(BusinessComponent bc, DataResponseDTO dto) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	@Override
	public long count(BusinessComponent bc) {
		throw new UnsupportedOperationException(errorMessage("error.unsupported_operation"));
	}

	protected MetaDTO buildMeta(List<FieldDTO> fields) {
		return buildMeta(fields, new ActionsDTO());
	}

	protected MetaDTO buildMeta(List<FieldDTO> fields, ActionsDTO actions) {
		return new MetaDTO(new RowMetaDTO(actions, FieldsDTO.of(fields)));
	}

}
