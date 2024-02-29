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

package org.cxbox.core.dto;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.dto.ScreenResponsibility;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;
import lombok.Getter;


@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
public class LoggedUser {

	private String sessionId;

	private Number userId;

	private String login;

	private String lastName;

	private String firstName;

	private String patronymic;

	private String fullName;

	private String principalName;

	private String phone;

	private String activeRole;

	private List<SimpleDictionary> roles;

	private List<ScreenResponsibility> screens;

	private JsonNode userSettingsVersion;

	private Collection<SimpleDictionary> featureSettings;

	private String systemUrl;

	private String timezone;

	private String language;

	private boolean devPanelEnabled;

}
