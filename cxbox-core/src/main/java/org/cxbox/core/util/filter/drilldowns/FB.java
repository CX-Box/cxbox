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

package org.cxbox.core.util.filter.drilldowns;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.dictionary.Dictionary;
import org.jetbrains.annotations.NotNull;


/**
 * This interface provided default cxbox fields
 * <br>We keep class name very short
 * to make inline highlights in IntelliJ IDEA non-disturbing in the most common usage case
 *
 *
 *<pre>If you want create  own implementaion please use {@link CxboxFB} insead {@link FB}  </>
 * @param <D> {@code <DTO extends DataResponse>} DTO which fields will be filtered
 * @param <S> {@SELF extends FilterBuilder<D, SELF>>} Filter builder implementation
 */
public interface FB<D extends DataResponseDTO, S extends FB<D, S>> {

	S input(@NonNull DtoField<? super D, String> field, @Nullable String value);

	<T extends Dictionary> S dictionary(@NonNull DtoField<? super D, T> field, @Nullable T value);

	<T extends Enum<?>> S dictionaryEnum(@NonNull DtoField<? super D, T> field, @Nullable T value);

	S date(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate value);

	S dateFromTo(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate from,
			@Nullable LocalDate to);

	S dateTime(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value);

	S dateTimeFromTo(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDateTime from,
			@Nullable LocalDateTime to);

	S multiValue(@NonNull DtoField<? super D, MultivalueField> field, @Nullable MultivalueField value);

	<T extends Number> S number(@NonNull DtoField<? super D, T> field, T value);

	<T extends Number> S numberFromTo(@NonNull DtoField<? super D, T> field, @Nullable T from, @Nullable T to);

	<T extends Number> S percent(@NonNull DtoField<? super D, T> field, T value);

	<T extends Number> S percentFromTo(@NonNull DtoField<? super D, T> field, T from, T to);

	S text(@NonNull DtoField<? super D, String> field, String value);

	<T extends Enum<?>> S radio(@NotNull DtoField<? super D, T> field, T... values);

	S checkbox(@NotNull DtoField<? super D, Boolean> field, boolean value);

	<T extends Number> S money(@NotNull DtoField<? super D, T> field, T value);

	<T extends Number> S moneyFromTo(@NotNull DtoField<? super D, T> field, T from, T to);

	<T extends Serializable> S fileUpload(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> S pickList(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> S inlinePickList(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> S multifield(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> S suggestionPickList(@NotNull DtoField<? super D, T> field, T value);

	S multivalueHover(@NotNull DtoField<? super D, MultivalueField> field, MultivalueField value);

	<T extends MultivalueField> S multipleSelect(@NotNull DtoField<? super D, T> field, T value);

}
