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

package org.cxbox.core.crudma.impl;

import java.util.Objects;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PessimisticLockException;
import javax.persistence.metamodel.SingularAttribute;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dictionary.CoreDictionaries.SystemPref;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.exception.UnableToLockException;
import org.cxbox.core.exception.VersionMismatchException;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.model.core.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
public abstract class VersionAwareResponseService<T extends DataResponseDTO, E extends BaseEntity> extends
		AbstractResponseService<T, E> {

	@Autowired
	private SystemSettings systemSettings;

	public VersionAwareResponseService(Class<T> typeOfDTO, Class<E> typeOfEntity,
			SingularAttribute<? super E, ? extends BaseEntity> parentSpec,
			Class<? extends FieldMetaBuilder<T>> metaBuilder) {
		super(typeOfDTO, typeOfEntity, parentSpec, metaBuilder);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CreateResult<T> createEntity(BusinessComponent bc) {
		// todo: add a check that the service returns actual data
		final E entity = create(bc);
		if (entity.getId() == null && bc.getId() != null) {
			entity.setId(bc.getIdAsLong());
		}
		final CreateResult<T> createResult = doCreateEntity(entity, bc);
		baseDAO.flush();
		baseDAO.refresh(entity);
		return createResult;
	}

	@SneakyThrows
	protected E create(BusinessComponent bc) {
		return typeOfEntity.newInstance();
	}

	@Override
	public ActionResultDTO<T> updateEntity(BusinessComponent bc, DataResponseDTO data) {
		// todo: добавить проверку что сервис возвращает актуальные данные
		return doUpdateEntity(loadEntity(bc, data), typeOfDTO.cast(data), bc);
	}

	@Override
	public ActionResultDTO<T> preview(BusinessComponent bc, DataResponseDTO data) {
		// todo: добавить проверку что сервис возвращает актуальные данные
		return doPreview(loadEntity(bc, data), typeOfDTO.cast(data), bc);
	}

	@Override
	protected E loadEntity(BusinessComponent bc, DataResponseDTO data) {
		E entity = isExist(bc.getIdAsLong());
		if (!Objects.equals(data.getVstamp(), -1L) && !Objects.equals(entity.getVstamp(), data.getVstamp())) {
			throw new VersionMismatchException(entity, data);
		}
		try {
			baseDAO.lock(entity, LockModeType.PESSIMISTIC_WRITE, getLockTimeout());
		} catch (OptimisticLockException | PessimisticLockException ex) {
			log.error(ex.getLocalizedMessage(), ex);
			// данные нам больше не нужны
			baseDAO.clear();
			entity = isExist(bc.getIdAsLong());
			throw new VersionMismatchException(entity, data);
		} catch (LockTimeoutException ex) {
			log.error(ex.getLocalizedMessage(), ex);
			throw new UnableToLockException();
		}
		return entity;
	}

	protected abstract CreateResult<T> doCreateEntity(E entity, BusinessComponent bc);

	protected abstract ActionResultDTO<T> doUpdateEntity(E entity, T data, BusinessComponent bc);

	protected ActionResultDTO<T> doPreview(E entity, T data, BusinessComponent bc) {
		return doUpdateEntity(entity, data, bc);
	}

	protected int getLockTimeout() {
		return systemSettings.getIntegerValue(SystemPref.UI_LOCK_TIMEOUT, -1);
	}

}
