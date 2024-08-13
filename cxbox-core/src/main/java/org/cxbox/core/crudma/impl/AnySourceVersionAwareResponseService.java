/*
 * © OOO "SI IKS LAB", 2022-2024
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.exception.AnySourceVersionMismatchException;

@Slf4j
public abstract class AnySourceVersionAwareResponseService<T extends DataResponseDTO, E> extends
		AbstractAnySourceResponseService<T, E> {

	protected AnySourceVersionAwareResponseService(Class<T> typeOfDTO, Class<E> typeOfEntity) {
		super(typeOfDTO, typeOfEntity);
	}

	protected AnySourceVersionAwareResponseService() {
		super();
	}

	public Long getVstamp(final E entity) {
		return 0L;
	}

	public void setVstamp(final String id, final E entity) {

	}

	/**
	 * При переопределении метода обязательно добавить сохранение результата в First Level Cache - getBaseDao().setWithFirstLevelCache()
	 * @param bc businessComponent
	 * @return
	 */
	@Override
	public CreateResult<T> createEntity(BusinessComponent bc) {
		// todo: add a check that the service returns actual data
		final E entity = create(bc);
		if (getDao().getId(entity) == null && bc.getId() != null) {
			getDao().setId(bc.getId(), entity);
		}
		if (getDao().getId(entity) == null) {
			getDao().setId(getDao().generateId(), entity);
		}
		final CreateResult<T> createResult = doCreateEntity(entity, bc);
		getDao().setWithFirstLevelCache(bc, entity);
//		baseDAO.flush();
//		baseDAO.refresh(entity);
		return createResult;
	}

	@SneakyThrows
	protected E create(BusinessComponent bc) {
		return typeOfEntity.newInstance();
	}

	/**
	 * При переопределении метода обязательно добавить сохранение результата в First Level Cache - getBaseDao().setWithFirstLevelCache()
	 * @param bc businessComponent
	 * @return
	 */
	@Override
	public ActionResultDTO<T> updateEntity(BusinessComponent bc, DataResponseDTO data) {
		// todo: добавить проверку что сервис возвращает актуальные данные
		final E entity = loadEntity(bc, data);
		final ActionResultDTO<T> resultDTO = doUpdateEntity(entity, typeOfDTO.cast(data), bc);
		getDao().setWithFirstLevelCache(bc, entity);
		return resultDTO;
	}

	@Override
	public ActionResultDTO<T> preview(BusinessComponent bc, DataResponseDTO data) {
		// todo: добавить проверку что сервис возвращает актуальные данные
		return doPreview(loadEntity(bc, data), typeOfDTO.cast(data), bc);
	}

	@Override
	protected E loadEntity(BusinessComponent bc, DataResponseDTO data) {
		E entity = isExist(bc);
		final Long vstamp = getVstamp(entity);
		if (!Objects.equals(data.getVstamp(), -1L) && !Objects.equals(vstamp, data.getVstamp())) {
			throw new AnySourceVersionMismatchException(vstamp, data);
		}
		//TODO переделать на lock на внутренней таблице BFF с двумя колонками - id сущности и название сущности
//		try {
//			baseDAO.lock(entity, LockModeType.PESSIMISTIC_WRITE, getLockTimeout());
//		} catch (OptimisticLockException | PessimisticLockException ex) {
//			log.error(ex.getLocalizedMessage(), ex);
//			// данные нам больше не нужны
//			baseDAO.clear();
//			entity = isExist(bc.getIdAsLong());
//			throw new VersionMismatchException(entity, data);
//		} catch (LockTimeoutException ex) {
//			log.error(ex.getLocalizedMessage(), ex);
//			throw new UnableToLockException();
//		}
		return entity;
	}

	protected abstract CreateResult<T> doCreateEntity(E entity, BusinessComponent bc);

	protected abstract ActionResultDTO<T> doUpdateEntity(E entity, T data, BusinessComponent bc);

	protected ActionResultDTO<T> doPreview(E entity, T data, BusinessComponent bc) {
		return doUpdateEntity(entity, data, bc);
	}

//	protected int getLockTimeout() {
//		return systemSettings.getIntegerValue(SystemPref.UI_LOCK_TIMEOUT, -1);
//	}

}
