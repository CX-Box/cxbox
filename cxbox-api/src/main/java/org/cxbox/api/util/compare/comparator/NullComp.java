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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cxbox.api.util.compare.comparator;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NullComp<E> implements Comparator<E> {

	private final Comparator<? super E> nonNullComparator;

	private final boolean nullsAreHigh;

	@Override
	public int compare(final E o1, final E o2) {
		if (o1 == o2) {
			return 0;
		}
		if (o1 == null) {
			return this.nullsAreHigh ? 1 : -1;
		}
		if (o2 == null) {
			return this.nullsAreHigh ? -1 : 1;
		}
		return this.nonNullComparator.compare(o1, o2);
	}


	@Override
	public int hashCode() {
		return (nullsAreHigh ? -1 : 1) * nonNullComparator.hashCode();
	}


	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!obj.getClass().equals(this.getClass())) {
			return false;
		}

		final NullComp<?> other = (NullComp<?>) obj;

		return this.nullsAreHigh == other.nullsAreHigh &&
				this.nonNullComparator.equals(other.nonNullComparator);
	}

}
