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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.exception.ServerException;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.AnySourceCrudmaImplementation;
import org.cxbox.core.crudma.impl.inner.AnySourceCrudmaService;
import org.cxbox.core.dto.PreInvokeEvent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.exception.EntityNotFoundException;
import org.cxbox.core.exception.UnconfirmedException;
import org.cxbox.core.dao.AnySourceBaseDAO;
import org.cxbox.core.service.rowmeta.AnySourceFieldMetaBuilder;
import org.cxbox.core.service.AnySourceDTOMapper;
import org.cxbox.core.service.AnySourceResponseService;
import org.cxbox.core.service.action.ActionDescription;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.action.AssocPreActionEventParameters;
import org.cxbox.core.service.action.DataResponsePreActionEventParameters;
import org.cxbox.core.service.action.PreActionCondition;
import org.cxbox.core.service.action.PreActionConditionHolderAssoc;
import org.cxbox.core.service.action.PreActionConditionHolderDataResponse;
import org.cxbox.core.service.action.PreActionEvent;
import org.cxbox.core.service.action.PreActionEventChecker;
import org.cxbox.core.service.rowmeta.RowMetaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@AnySourceCrudmaImplementation(AnySourceCrudmaService.class)
public abstract class AbstractAnySourceResponseService<T extends DataResponseDTO, E> implements
		AnySourceResponseService<T, E> {

	@Getter
	protected final Class<T> typeOfDTO;

	@Getter
	protected final Class<E> typeOfEntity;

	private final Class<? extends AnySourceFieldMetaBuilder<T>> metaBuilder;

	@Getter
	protected final Class<? extends AnySourceBaseDAO<E>> anySourceBaseDAOClass;

	protected Class<? extends PreActionConditionHolderDataResponse<T>> preActionConditionHolderDataResponse = null;

	protected Class<? extends PreActionConditionHolderAssoc> preActionConditionHolderAssoc = null;

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	private AnySourceDTOMapper dtoMapper;

	@Autowired
	private List<AnySourceBaseDAO<E>> anySourceBaseDAOs;

	@Override
	public AnySourceBaseDAO<E> getBaseDao() {
		return anySourceBaseDAOs.stream().filter(dao -> anySourceBaseDAOClass.isAssignableFrom(dao.getClass())).findFirst()
				.orElseThrow();
	}

	public static <T> T cast(Object o, Class<T> clazz) {
		return clazz.isInstance(o) ? clazz.cast(o) : null;
	}

	/**
	 * Saving the value of the DTO field (when it changes) in the entity field (using the custom DTO-getter).
	 *
	 * @param <D> type of DTO field value to be saved in the entity field
	 * @param <V> type of entity field to the value is to be saved
	 * @param dto DTO-object, which value to be saved to the entity field
	 * @param dtoField the DTO-object field, which value to be saved to the entity field
	 * @param entitySetter method for saving a value (when it changes) to an entity
	 * @param dtoGetter method for retrieving a value (when it changes) from the DTO
	 * @param mapper converts the saving value into the corresponding entity field type
	 */
	public final <D, V> void setMappedIfChanged(
			final T dto, final DtoField<? super T, D> dtoField,
			final Consumer<V> entitySetter, final Supplier<D> dtoGetter, final Function<D, V> mapper) {
		if (dto.isFieldChanged(dtoField)) {
			entitySetter.accept(mapper.apply(dtoGetter.get()));
		}
	}

	/**
	 * Saving the value of the DTO field (when it changes) in the entity field (using the custom DTO-getter).
	 *
	 * @param <V> type of entity field to the value is to be saved
	 * @param dto DTO-object, which value to be saved to the entity field
	 * @param dtoField the DTO-object field, which value to be saved to the entity field
	 * @param entitySetter method for saving a value (when it changes) to an entity
	 * @param dtoGetter method for retrieving a value (when it changes) from the DTO
	 */
	public final <V> void setIfChanged(
			final T dto, final DtoField<? super T, V> dtoField,
			final Consumer<V> entitySetter, final Supplier<V> dtoGetter) {
		setMappedIfChanged(dto, dtoField, entitySetter, dtoGetter, Function.identity());
	}

	/**
	 * Saving the value of the DTO field (when it changes) in the entity field.
	 *
	 * @param <D> type of DTO field value to be saved in the entity field
	 * @param <V> type of entity field to the value is to be saved
	 * @param dto DTO-object, which value to be saved to the entity field
	 * @param dtoField the DTO-object field, which value to be saved to the entity field
	 * @param entitySetter method for saving a value (when it changes) to an entity
	 * @param mapper converts the saving value into the corresponding entity field type
	 */
	public final <D, V> void setMappedIfChanged(
			final T dto, final DtoField<? super T, D> dtoField,
			final Consumer<V> entitySetter, final Function<D, V> mapper) {
		setMappedIfChanged(dto, dtoField, entitySetter, () -> dtoField.getValue(dto), mapper);
	}

	/**
	 * Saving the value of the DTO field (when it changes) in the entity field.
	 *
	 * @param <V> type of entity field to the value is to be saved
	 * @param dto DTO-object, which value to be saved to the entity field
	 * @param dtoField the DTO-object field, which value to be saved to the entity field
	 * @param entitySetter method for saving a value (when it changes) to an entity
	 */
	public final <V> void setIfChanged(final T dto, final DtoField<? super T, V> dtoField,
			final Consumer<V> entitySetter) {
		setMappedIfChanged(dto, dtoField, entitySetter, Function.identity());
	}

	@Override
	public <V> V unwrap(Class<V> cls) {
		if (cls.isInstance(this)) {
			return (V) this;
		}
		throw new IllegalArgumentException(cls.getName());
	}

	@Override
	public boolean isDeferredCreationSupported(BusinessComponent bc) {
		return true;
	}

	@Override
	public boolean hasPersister() {
		return !typeOfEntity.isInterface() && !Modifier.isAbstract(typeOfEntity.getModifiers());
	}

	@Override
	public E getOneAsEntity(BusinessComponent bc) {
		return getBaseDao().getById(bc);
	}

	@Override
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = CacheConfig.REQUEST_CACHE,
			key = "{#root.targetClass, #root.methodName, #bc.name, #bc.id}"
	)
	public T getOne(BusinessComponent bc) {
		return doGetOne(bc);
	}

	protected T doGetOne(BusinessComponent bc) {
		return entityToDto(bc, getOneAsEntity(bc));
	}

	public ActionResultDTO<T> deleteEntity(BusinessComponent bc) {
		getBaseDao().delete(bc);
		return new ActionResultDTO<>();
	}

	@Override
	public ResultPage<T> getList(BusinessComponent bc) {
		Page<E> page = getBaseDao().getList(bc, bc.getParameters());
		return entitiesToDtos(
				bc,
				ResultPage.of(page.stream().toList(), page.hasNext())
		);
	}

	@Override
	public ActionsDTO getAvailableActions(RowMetaType metaType, DataResponseDTO data, BusinessComponent bc) {
		return getActions().toDto(bc);
	}

	@Override
	public ActionResultDTO onCancel(BusinessComponent bc) {
		return new ActionResultDTO().setAction(PostAction.postDelete());
	}

	@Override
	public ActionResultDTO<T> invokeAction(BusinessComponent bc, String actionName, DataResponseDTO data) {
		ActionDescription<T> action = getActions().getAction(actionName);
		if (action == null || !action.isAvailable(bc)) {
			throw new BusinessException().addPopup(
					errorMessage("error.action_unavailable", actionName)
			);
		}
		preInvoke(bc, action.withPreActionEvents(bc), data, null);
		T record = null;
		if (nonNull(bc.getId())) {
			// Data is changed and we need to apply these changes
			// Lock must be set here
			if (action.isAutoSaveBefore() && nonNull(data) && data.hasChangedFields()) {
				record = updateEntity(bc, data).getRecord();
			} else {
				// No changes comes,
				// but action requires lock
				if (action.isUpdateRequired() && hasPersister()) {
					loadEntity(bc, data);
				}
				// WARNING! Don't touch cache here!
				// getOne() method may not be invoked
				record = doGetOne(bc);
			}
		}
		return action.invoke(bc, Optional.ofNullable(record).orElse((T) data));
	}


	private void preInvoke(BusinessComponent bc, List<PreActionEvent> preActionEvents, DataResponseDTO data,
			AssociateDTO associateDTO) {
		List<String> preInvokeParameters = bc.getPreInvokeParameters();
		List<PreInvokeEvent> preInvokeEvents = new ArrayList<>();
		if (nonNull(preActionEvents)) {
			preActionEvents.forEach(preActionEvent -> {
				if (nonNull(preActionEvent) && !preInvokeParameters.contains(preActionEvent.getKey()) &&
						(data == null ? getCheckerAssoc(preActionEvent.getPreActionCondition())
								.check(new AssocPreActionEventParameters(associateDTO, bc, preInvokeParameters))
								: getCheckerData(preActionEvent.getPreActionCondition())
										.check(new DataResponsePreActionEventParameters(data, bc, preInvokeParameters)))) {
					preInvokeEvents.add(PreInvokeEvent.of(
							preActionEvent.getKey(),
							preActionEvent.getType().getKey(),
							preActionEvent.getMessage()
					));
				}
			});
		}
		if (!preInvokeEvents.isEmpty()) {
			throw new UnconfirmedException().addPreInvokeEvents(preInvokeEvents);
		}
	}

	private PreActionEventChecker<T> getCheckerData(PreActionCondition preActionCondition) {
		if (nonNull(preActionConditionHolderDataResponse)) {
			PreActionEventChecker<T> checker = applicationContext
					.getBean(preActionConditionHolderDataResponse).getChecker(preActionCondition);
			if (nonNull(checker)) {
				return checker;
			}
			throw new ServerException(
					"PreActionHolder in " + getClass().getSimpleName() + "doesn't have checker for " + preActionCondition
							.getName() + "preAction");
		}
		throw new ServerException(
				"PreActionConditionHolder is null for " + preActionCondition.getName() + " preaction in " + getClass()
						.getSimpleName() + " service");
	}

	private PreActionEventChecker<AssociateDTO> getCheckerAssoc(PreActionCondition preActionCondition) {
		if (nonNull(preActionConditionHolderAssoc)) {
			PreActionEventChecker<AssociateDTO> checker = applicationContext.getBean(preActionConditionHolderAssoc)
					.getChecker(preActionCondition);
			if (nonNull(checker)) {
				return checker;
			}
			throw new ServerException(
					"PreActionHolder in " + getClass().getSimpleName() + "doesn't have checker for " + preActionCondition
							.getName() + "preAction");
		}
		throw new ServerException(
				"PreActionConditionHolder is null for " + preActionCondition.getName() + " preaction in " + getClass()
						.getSimpleName() + " service");
	}

	@Override
	public long count(BusinessComponent bc) {
		return getBaseDao().count(bc);
	}

	@Override
	public void validate(BusinessComponent bc, DataResponseDTO data) {
		T entityDto = entityToDto(bc, getOneAsEntity(bc));
		updateDataDto(data, entityDto);
		ActionDescription<T> save = getActions().getAction(ActionType.SAVE.getType());
		if (nonNull(save)) {
			popup(save.validate(bc, data, entityDto));
			List<PreActionEvent> preActionEvents = save.withPreActionEvents(bc);
			preInvoke(bc, nonNull(preActionEvents) ? preActionEvents : getPreActionsForSave(),
					data, null
			);
		}
	}

	private void popup(List<String> messages) {
		if (nonNull(messages) && !messages.isEmpty()) {
			throw new BusinessException().addPopup(messages);
		}
	}

	public Actions<T> getActions() {
		return Actions.<T>builder()
				.action("drillDown", "Посмотреть форму")
				.available(bc -> false).invoker((bc, data) -> null).add(false)
				.build();
	}

	protected List<PreActionEvent> getPreActionsForSave() {
		return Collections.emptyList();
	}

	protected ResultPage<T> entitiesToDtos(BusinessComponent bc, ResultPage<E> entities) {
		return ResultPage.of(entities, e -> entityToDto(bc, e));
	}

	@Override
	public T entityToDto(final BusinessComponent bc, final E entity) {
		return dtoMapper.entityToDto(bc, entity, typeOfDTO);
	}

	private void updateDataDto(DataResponseDTO data, T entityDto) {
		T updatedDto = cast(data, typeOfDTO);
		Stream.of(entityDto.getClass().getDeclaredFields())
				.filter(field -> !data.isFieldChanged(field.getName()))
				.forEach(field -> {
					field.setAccessible(true);
					try {
						field.set(updatedDto, getValue(field.getName(), entityDto));
					} catch (IllegalAccessException e) {
						log.error(e.getLocalizedMessage());
					}
				});
	}

	private Object getValue(String fieldName, T data) {
		if (isNull(data)) {
			return null;
		}
		AtomicReference<Object> value = new AtomicReference<>();
		Stream.of(data.getClass().getDeclaredFields())
				.filter(field -> field.getName().equals(fieldName))
				.findFirst()
				.ifPresent(field -> {
					field.setAccessible(true);
					try {
						value.set(field.get(data));
					} catch (IllegalAccessException e) {
						log.error(e.getLocalizedMessage());
					}
				});
		return value.get();
	}

	/**
	 * deprecated, the hasNext formation logic has been moved to the DAO layer
	 * Left for custom DAOs that return List instead of ResultPage
	 */
	@Deprecated
	protected ResultPage<E> entityListToResultPage(final List<E> entities, final int limit) {
		boolean hasNext;
		int size = entities.size();
		if (size == (limit + 1)) {
			entities.remove(size - 1);
			hasNext = true;
		} else {
			hasNext = false;
		}
		return new ResultPage<>(entities, hasNext);

	}

	protected ResultPage<T> dtoListToResultPage(final List<T> dtos, final int limit) {
		boolean hasNext;
		int size = dtos.size();
		if (size == (limit + 1)) {
			dtos.remove(size - 1);
			hasNext = true;
		} else {
			hasNext = false;
		}
		return new ResultPage<>(dtos, hasNext);
	}

	protected final E isExist(final BusinessComponent bc) {
		E entity = getBaseDao().getById(bc);
		if (entity == null) {
			throw new EntityNotFoundException(typeOfEntity.getSimpleName(), bc.getIdAsLong());
		}
		return entity;
	}

	protected E loadEntity(BusinessComponent bc, DataResponseDTO data) {
		return getOneAsEntity(bc);
	}

	public Class<? extends AnySourceFieldMetaBuilder<T>> getAnySourceFieldMetaBuilder() {
		return this.metaBuilder;
	}

	@Override
	public ActionResultDTO<T> updateEntity(BusinessComponent bc, DataResponseDTO data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ActionResultDTO<T> preview(BusinessComponent bc, DataResponseDTO data) {
		return updateEntity(bc, data);
	}

	@Override
	public CreateResult<T> createEntity(BusinessComponent bc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public AssociateResultDTO associate(List<AssociateDTO> data, BusinessComponent bc) {
		ActionDescription<T> associate = getActions().getAction(ActionType.ASSOCIATE.getType());
		if (nonNull(associate)) {
			data.stream().filter(AssociateDTO::getAssociated)
					.forEach(dto -> preInvoke(bc, associate.withPreActionEvents(bc), null, dto));
		}
		return doAssociate(data, bc);
	}

	protected AssociateResultDTO doAssociate(List<AssociateDTO> data, BusinessComponent bc) {
		throw new UnsupportedOperationException();
	}

}
