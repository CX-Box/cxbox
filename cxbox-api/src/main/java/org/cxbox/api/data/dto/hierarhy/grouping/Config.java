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

import java.util.Map;
import lombok.Getter;

@Getter
//@Builder
public class Config {

	private Map<String, String> options;

	//@Builder de-lombok generated code
	@java.beans.ConstructorProperties({"options"})
	Config(Map<String, String> options) {
		this.options = options;
	}

	public static Cfg builder() {
		return new Cfg();
	}

	public static class Cfg {

		private Map<String, String> options;

		Cfg() {
		}

		/**
		 * <br>
		 * <br>
		 * @param options -- params of current hierarchy subtree root. Designed to be used for project customizations of "GroupingHierarchy" widget (for example to set custom property "expandedByDefault": true)
		 * @return this
		 */
		public Cfg options(Map<String, String> options) {
			this.options = options;
			return this;
		}

		public Config build() {
			return new Config(this.options);
		}

		public String toString() {
			return "Config.ConfigBuilder(options=" + this.options + ")";
		}

	}

}
