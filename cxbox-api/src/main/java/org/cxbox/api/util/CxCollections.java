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

package org.cxbox.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CxCollections {

	public static <T> Collector<T, ?, List<T>> toSortedList(Comparator<? super T> c) {
		return Collectors.collectingAndThen(Collectors.toList(), list -> {
			list.sort(c);
			return list;
		});
	}

	@NonNull
	public static <T> List<List<T>> partition(@NonNull List<T> list, int size) {
		List<List<T>> partitions = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			partitions.add(list.subList(i, Math.min(i + size, list.size())));
		}
		return partitions;
	}

	@NonNull
	public static <T> Stream<T> stream(@NonNull Iterable<T> iterable) {
		return (iterable instanceof Collection)
				? ((Collection<T>) iterable).stream()
				: StreamSupport.stream(iterable.spliterator(), false);
	}

	@NonNull
	public static <E> Set<E> intersection(
			final @NonNull Set<E> set1, final @NonNull Set<? extends E> set2) {
		Set<E> intersectSet = new HashSet<>(set1);
		intersectSet.retainAll(set2);
		return intersectSet;
	}

	@NonNull
	public static List<String> split(@NonNull String text, int n) {
		List<String> results = new ArrayList<>();
		int length = text.length();

		for (int i = 0; i < length; i += n) {
			results.add(text.substring(i, Math.min(length, i + n)));
		}

		return results;
	}
}
