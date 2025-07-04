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


public interface FilterBuilder<D extends DataResponseDTO, SELF extends FilterBuilder<D, SELF>> {


	SELF input(@NonNull DtoField<? super D, String> field, @Nullable String value);

	<T extends Dictionary> SELF dictionary(@NonNull DtoField<? super D, T> field, @Nullable T value);

	<T extends Enum<?>> SELF dictionaryEnum(@NonNull DtoField<? super D, T> field, @Nullable T value);

	SELF date(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate value);

	SELF dateFromTo(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate from,
			@Nullable LocalDate to);

	SELF dateTime(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value);

	SELF dateTimeFromTo(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDateTime from,
			@Nullable LocalDateTime to);


	SELF multiValue(@NonNull DtoField<? super D, MultivalueField> field, @Nullable MultivalueField value);


	<T extends Number> SELF number(@NonNull DtoField<? super D, T> field, T value);

	<T extends Number> SELF numberFromTo(@NonNull DtoField<? super D, T> field, @Nullable T from, @Nullable T to);

	<T extends Number> SELF percent(@NonNull DtoField<? super D, T> field, T value);

	<T extends Number> SELF percentFromTo(@NonNull DtoField<? super D, T> field, T from, T to);

	SELF text(@NonNull DtoField<? super D, String> field, String value);

	<T extends Enum<?>> SELF radio(@NotNull DtoField<? super D, T> field, T... values);

	SELF checkbox(@NotNull DtoField<? super D, Boolean> field, boolean value);

	<T extends Number> SELF money(@NotNull DtoField<? super D, T> field, T value);

	<T extends Number> SELF moneyFromTo(@NotNull DtoField<? super D, T> field, T from, T to);


	<T extends Serializable> SELF fileUpload(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> SELF pickList(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> SELF inlinePickList(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> SELF multifield(@NotNull DtoField<? super D, T> field, T value);

	<T extends Serializable> SELF suggestionPickList(@NotNull DtoField<? super D, T> field, T value);

	SELF multivalueHover(@NotNull DtoField<? super D, MultivalueField> field, MultivalueField value);


	<T extends MultivalueField> SELF multipleSelect(@NotNull DtoField<? super D, T> field, T value);


}
