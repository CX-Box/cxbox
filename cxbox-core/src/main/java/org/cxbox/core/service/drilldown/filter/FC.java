/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.core.service.drilldown.filter;


import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;


/**
 * {@link FC} is a builder and container for filter configurations associated with business components (BC).
 * <br>Short class {@link FC} (Filter configuration) name ensures non-intrusive IntelliJ IDEA inline highlights.
 * <p>
 * It allows adding filter builders for specific DTO classes and BC identifiers,
 * supporting default and custom filter builder types.
 * </p>
 *
 * <pre>
 * {@code
 * FC fc = new FC();
 * fc.add(bcIdentifier1, bcIdentifier1DTO.class, fb ->
 * 	fb.input(bcIdentifier1DTO_.input, "value")
 * 	// and other needed filed
 * )
 * .add(bcIdentifier2, BcIdentifier2DTO.class,
 *  new TypeToken<MyFilterBuilder<BcIdentifier2DTO>>() {},
 *  fb -> fb
 *     .customField(bcIdentifier2DTO_.customField, value)
 *     .input(bcIdentifier2DTO_.input, "value")
 * );
 * }
 * </pre>
 */
@Getter
public class FC {

	private final List<FCR> FCRs = new ArrayList<>();

	/**
	 * Adds a default filter builder for the specified BC identifier and DTO class.
	 * <pre>
	 * {@code
	 * FC fc = new FC();
	 * fc.add(bcIdentifier1, MyDto1.class, fb -> {
	 *     // configure builder
	 * })
	 * }
	 *  * </pre>
	 *
	 * @param bc        the business component identifier {@link BcIdentifier}
	 * @param dtoClass  the DTO class associated with the filter {@link DataResponseDTO}
	 * @param consumer  a consumer for configuring the filter builder {@link FB<D,?>}
	 * @param <D>       the type of the DTO  {@link DataResponseDTO}
	 * @return this FC instance for chaining
	 */
	public <D extends DataResponseDTO> FC add(BcIdentifier bc, Class<D> dtoClass,
			Consumer<FB<D,?>> consumer) {
		FB<D,?> fb = new FB<>();
		consumer.accept(fb);
		FCR fcr = new FCR(bc, fb.getFieldFilters());
		FCRs.add(fcr);
		return this;
	}

	/**
	 * Adds a custom filter builder for the specified BC identifier and DTO class using a type token.
	 *
	 * <pre>
	 * {@code
	 * FC fc = new FC();
	 * fc.add(myBc, MyDto.class,  new TypeToken<MyFilterBuilder<MyDto>>() {} , fb -> {
	 *     // configure builder
	 * }
	 * }
	 * </pre>
	 * @param bc        the business component identifier {@link BcIdentifier}
	 * @param dtoClass  the DTO class associated with the filter {@link DataResponseDTO}
	 * @param token     the type token for the custom filter builder {@link TypeReference}
	 * @param filterBuilder  a consumer for configuring the custom filter builder {@link FB}
	 * @param <D>       the type of the DTO {@link DataResponseDTO}
	 * @param <F>       the type of the filter builder {@link FB}
	 * @return this FC instance for chaining
	 */
	@SuppressWarnings("unchecked")
	public <D extends DataResponseDTO, F extends FB<D, F>> FC add(
			BcIdentifier bc, Class<D> dtoClass, TypeReference<F> token, Consumer<F> filterBuilder) {
		try {
			Type type = token.getType();
			Class<?> rawType = type instanceof Class
					? (Class<?>) type
					: (Class<?>) ((ParameterizedType) type).getRawType();
			F f = (F) rawType.getDeclaredConstructor().newInstance();
			filterBuilder.accept(f);
			FCR fcr = new FCR(bc, f.getFieldFilters());
			FCRs.add(fcr);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return this;
	}


	/**
	 * Record representing a filter configuration for a specific business component.
	 * <br>Short class name ensures non-intrusive IntelliJ IDEA inline highlights.
	 *
	 * @param bcIdentifier the business component identifier {@link BcIdentifier}
	 * @param filterStrings the collection with filter strings
	 */
	public record FCR(BcIdentifier bcIdentifier, Collection<String> filterStrings) {

	}

}

