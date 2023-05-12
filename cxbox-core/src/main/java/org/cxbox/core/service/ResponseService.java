
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

package org.cxbox.core.service;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.core.service.rowmeta.RowMetaType;
import org.cxbox.model.core.entity.BaseEntity;

import java.util.List;


public interface ResponseService<T extends DataResponseDTO, E extends BaseEntity> {

	/**
	 * Returns an entity based on a business component
	 *
	 * @param bc businessComponent
	 * @return {@link BaseEntity} entity
	 */
	BaseEntity getOneAsEntity(BusinessComponent bc);

	/**
	 * Returns object based on a business component
	 *
	 * @param bc businessComponent
	 * @return object
	 */
	T getOne(BusinessComponent bc);

	/**
	 * Determines whether the service interacts with an entity from the database
	 *
	 * @return true/false
	 */
	boolean hasPersister();

	/**
	 * Returns a list of matched objects based on a business component
	 *
	 * @param bc businessComponent
	 * @return list of matched objects
	 */
	ResultPage<T> getList(BusinessComponent bc);

	/**
	 * Creates an entity based on a business component
	 *
	 * @param bc businessComponent
	 * @return {@link CreateResult} class with DataResponseDTO and postactions
	 */
	CreateResult<T> createEntity(BusinessComponent bc);

	/**
	 * Updates an entity based on a business component by DTO
	 *
	 * @param bc businessComponent
	 * @param data information about entity, it's changed fields, errors
	 * @return {@link ActionResultDTO} class with DataResponseDTO and postactions
	 */
	ActionResultDTO<T> updateEntity(BusinessComponent bc, DataResponseDTO data);

	/**
	 * Updates an entity based on a business component by DTO
	 * Used in forceactive fields
	 *
	 * @param bc businessComponent
	 * @param data information about entity, it's changed fields, errors
	 * @return {@link ActionResultDTO} class with DataResponseDTO and postactions
	 */
	ActionResultDTO<T> preview(BusinessComponent bc, DataResponseDTO data);

	/**
	 * Deletes an entity based on a business component
	 *
	 * @param bc businessComponent
	 * @return {@link ActionResultDTO} class with DataResponseDTO and postactions
	 */
	ActionResultDTO<T> deleteEntity(BusinessComponent bc);

	/**
	 * Invokes action with given name, add preactions, loads or updates entity if necessary
	 *
	 * @param bc businessComponent
	 * @param actionName name of action
	 * @param data information about entity, it's changed fields, errors
	 * @return {@link ActionResultDTO} class with DataResponseDTO and postactions
	 */
	ActionResultDTO<T> invokeAction(BusinessComponent bc, String actionName, DataResponseDTO data);

	/**
	 * Сreates links between entities
	 *
	 * @param data information about an entity, whether the entity was associated
	 * @param bc businessComponent
	 * @return {@link AssociateResultDTO} class with DataResponseDTO and postactions
	 */
	AssociateResultDTO associate(List<AssociateDTO> data, BusinessComponent bc);

	/**
	 * Returns actions for entity with conditions of their availability
	 * invoke method getActions
	 *
	 * @param metaType type of meta
	 * @param data information about entity, it's changed fields, errors
	 * @param bc businessComponent
	 * @return {@link AssociateResultDTO} class with DataResponseDTO and postactions
	 */
	ActionsDTO getAvailableActions(RowMetaType metaType, DataResponseDTO data, BusinessComponent bc);

	/**
	 * Returns actions for entity with conditions of their availability
	 * invoked by method getAvailableActions
	 *
	 * @return {@link AssociateResultDTO} class with DataResponseDTO and postactions
	 */
	Actions<T> getActions();

	/**
	 * Returns actions invoked with cancel
	 *
	 * @param bc businessComponent
	 * @return {@link AssociateResultDTO} class with DataResponseDTO and postactions
	 */
	ActionResultDTO onCancel(BusinessComponent bc);

	/**
	 * Returns FieldMetaBuilder for class
	 *
	 * @return {@link FieldMetaBuilder} class for building field meta
	 */
	Class<? extends FieldMetaBuilder<T>> getFieldMetaBuilder();

	/**
	 * Returns the number of matching entities
	 *
	 * @param bc businessComponent
	 * @return count
	 */
	long count(BusinessComponent bc);

	/**
	 * Returns the number of matching entities
	 *
	 * @return count
	 */
	Class<T> getTypeOfDTO();

	/**
	 * Returns the number of matching entities
	 *
	 * @return count
	 */
	Class<E> getTypeOfEntity();

	/**
	 * Validates the entry on save
	 *
	 * @param bc businessComponent
	 * @param data information about entity, it's changed fields, errors
	 */
	void validate(BusinessComponent bc, DataResponseDTO data);

	@Deprecated
	<V> V unwrap(Class<V> cls);

	/**
	 * Determines is deferred saving of new objects supported
	 *
	 * @param bc businessComponent
	 */
	boolean isDeferredCreationSupported(BusinessComponent bc);

}
