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

package org.cxbox.core.service;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.dto.data.view.BusinessObjectDTO;
import org.cxbox.core.dto.data.view.ScreenNavigation;
import org.cxbox.core.dto.data.view.ScreenResponsibility;
import org.cxbox.model.core.entity.IUser;
import org.cxbox.model.ui.entity.BcProperties;
import org.cxbox.model.ui.entity.FilterGroup;
import org.cxbox.model.ui.entity.Screen;
import org.cxbox.model.ui.entity.View;
import org.cxbox.model.ui.entity.ViewWidgets;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;


public interface UIService {

	List<ScreenResponsibility> getCommonScreens();

	JsonNode getUserSettings();

	boolean isCommonScreen(String screenName);

	Map<String, Boolean> getResponsibilities(IUser<Long> user, LOV userRole);

	String getFirstViewFromResponsibilities(IUser<Long> user, LOV userRole, String... views);

	String getFirstViewFromResponsibilities(IUser<Long> user, String... views);

	List<String> getViews(String screenName, IUser<Long> user, LOV userRole);

	Screen findScreenByName(String name);

	ScreenNavigation getScreenNavigation(final Screen screen);

	List<View> getViews(final List<String> views);

	Map<String, List<ViewWidgets>> getAllWidgetsWithPositionByScreen(List<String> views);

	Map<String, List<FilterGroup>> getFilterGroups(BusinessObjectDTO boDto);

	Map<String, BcProperties> getStringDefaultBcPropertiesMap(BusinessObjectDTO boDto);

	void invalidateCache();

}
