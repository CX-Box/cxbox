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

package org.cxbox.core.dto.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.RequestValueCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalDtoConstructorService {

	private final RequestValueCache requestCache;

//	private final Map<Key, DtoConstructor<?, ? extends DataResponseDTO>> constructors;

	public ExternalDtoConstructorService(
			final RequestValueCache requestCache,
			@Autowired(required = false) final List<DtoConstructor<?, ? extends DataResponseDTO>> dtoConstructors) {
		this.requestCache = requestCache;
//		this.constructors = Optional.ofNullable(dtoConstructors)
//				.map(Collection::stream)
//				.map(s -> s.collect(Collectors.toMap(Key::new, Function.identity())))
//				.orElse(Collections.emptyMap());
	}

	@SneakyThrows
	public <E, D extends DataResponseDTO> D create(
			final E entity,
			final Class<D> dtoClass,
			final Collection<DtoField<D, ?>> fields,
			final Map<String, Object> attributes) {
		final D dto;
		//TODO раскопировать DtoConstructor и Mapping
//		final DtoConstructor<E, D> dtoConstructor = findConstructor(entity, dtoClass);
//		if (dtoConstructor != null) {
//			dto = ConstructorUtils.invokeConstructor(dtoClass);
//			dto.setId(entity.getId().toString());
//			final Mapping<E, D> mapping = new Mapping<>(requestCache, dtoConstructor.getValueSuppliers(), entity);
//			attributes.forEach(mapping::addAttribute);
//			for (final DtoField<D, ?> field : fields) {
//				final Optional<Object> value = mapping.get((DtoField<D, Object>) field);
//				if (value.isPresent()) {
//					FieldUtils.writeField(dto, field.getName(), value.get(), true);
//				}
//				dto.addComputedField(field.getName());
//			}
//		} else {
			dto = ConstructorUtils.invokeConstructor(dtoClass, entity);
//		}
		dto.setSerializableFields(
				fields.stream().map(DtoField::getName).collect(Collectors.toSet())
		);
		return dto;
	}

//	private <E extends BaseEntity, D extends DataResponseDTO> DtoConstructor<E, D> findConstructor(
//			final E entity,
//			final Class<D> dtoClass) {
//		Class entityClass = JpaUtils.unproxiedClass(entity);
//		do {
//			if (BaseEntity.class.isAssignableFrom(entityClass)) {
//				final DtoConstructor<E, D> dtoConstructor = (DtoConstructor<E, D>) constructors.get(
//						new Key(entityClass, dtoClass)
//				);
//				if (dtoConstructor != null) {
//					return dtoConstructor;
//				}
//			}
//			entityClass = entityClass.getSuperclass();
//		} while (!BaseEntity.class.equals(entityClass) && !Object.class.equals(entityClass) && entityClass != null);
//		return null;
//	}

//	@EqualsAndHashCode
//	@RequiredArgsConstructor
//	private static class Key {
//
//		private final Class<? extends BaseEntity> entityClass;
//
//		private final Class<? extends DataResponseDTO> dtoClass;
//
//		Key(DtoConstructor<? extends BaseEntity, ? extends DataResponseDTO> dtoConstructor) {
//			this(dtoConstructor.getEntityClass(), dtoConstructor.getDtoClass());
//		}
//
//	}

}
