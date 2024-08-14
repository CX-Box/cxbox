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

package org.cxbox.api.util.compare;

import java.util.Comparator;
import java.util.List;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.cxbox.api.util.compare.common.Transformer;
import org.cxbox.api.util.compare.comparator.ChainComp;
import org.cxbox.api.util.compare.comparator.ComparableComp;
import org.cxbox.api.util.compare.comparator.NullComp;
import org.cxbox.api.util.compare.comparator.ReverseComp;
import org.cxbox.api.util.compare.comparator.TransformingComp;

/*
	lightweight alternative to apache commons-collections4 dependency
 */
@UtilityClass
public class CxComparatorUtils {

	public static final Comparator NATURAL_COMPARATOR = ComparableComp.INSTANCE;

	@SuppressWarnings("unchecked")
	public static <E extends java.lang.Comparable<? super E>> Comparator<E> naturalComparator() {
		return NATURAL_COMPARATOR;
	}

	public static <E> Comparator<E> chainedComparator(@NonNull final List<Comparator<E>> comparators) {
		return new ChainComp<>(comparators);
	}


	public static <E> Comparator<E> reversedComparator(@NonNull final Comparator<E> comparator) {
		return new ReverseComp<>(comparator);
	}


	@SuppressWarnings("unchecked")
	public static <E> Comparator<E> nullHighComparator(Comparator<E> comparator) {
		if (comparator == null) {
			comparator = NATURAL_COMPARATOR;
		}
		return new NullComp<>(comparator, true);
	}


	@SuppressWarnings("unchecked")
	public static <I, O> Comparator<I> transformedComparator(Comparator<O> comparator,
			final Transformer<? super I, ? extends O> transformer) {

		if (comparator == null) {
			comparator = NATURAL_COMPARATOR;
		}
		return new TransformingComp<>(comparator, transformer);
	}

}
