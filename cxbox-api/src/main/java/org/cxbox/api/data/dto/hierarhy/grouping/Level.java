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

package org.cxbox.api.data.dto.hierarhy.grouping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = {"value"})
public class Level<T, G extends Level<?, ?>> implements Serializable {

	@JsonInclude
	private T value;

	@JsonInclude
	private Set<G> child;

	private Map<String, Object> options;

	@JsonIgnore
	//generated with @Builder de-lombok action code
	@java.beans.ConstructorProperties({"value", "child", "options"})
	Level(T value, Set<G> child, Map<String, Object> options) {
		this.value = value;
		this.child = child;
		this.options = options;
	}

	@JsonIgnore
	public static <T> LevelBuilder<T, ?> builder(T value) {
		LevelBuilder<T, ?> builder = Level.hiddenBuilder();
		return builder.value(value).child(null);
	}

	@JsonIgnore
	public static <T, G extends Level<?, ?>> LevelBuilder<T, G> builder(T value, Set<G> child) {
		LevelBuilder<T, G> builder = Level.hiddenBuilder();
		return builder.value(value).child(child);
	}

	@JsonIgnore
	public static <T, G extends Level<?, ?>> LevelBuilder<T, G> hiddenBuilder() {
		return new LevelBuilder<T, G>();
	}

	public static class LevelBuilder<T, G extends Level<?, ?>> {

		@JsonIgnore
		private T value;

		@JsonIgnore
		private Set<G> child;

		@JsonIgnore
		private Map<String, Object> options;

		@JsonIgnore
		LevelBuilder() {
		}

		@JsonIgnore
		public LevelBuilder<T, G> value(T value) {
			this.value = value;
			return this;
		}

		@JsonIgnore
		public LevelBuilder<T, G> child(Set<G> child) {
			this.child = child;
			return this;
		}

		@JsonIgnore
		public LevelBuilder<T, G> options(Map<String, Object> options) {
			this.options = options;
			return this;
		}

		@JsonIgnore
		public Level<T, G> build() {
			return new Level<T, G>(this.value, this.child, this.options);
		}

		@JsonIgnore
		public String toString() {
			return "FieldDTO.Level.LevelBuilder(value=" + this.value + ", child=" + this.child + ", options=" + this.options
					+ ")";
		}

	}

}
