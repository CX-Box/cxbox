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

package org.cxbox.model.core.service;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.tx.DeploymentTransactionSupport;
import org.cxbox.api.system.SystemSettingChangedEvent;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.entity.SystemSetting;
import org.cxbox.model.core.entity.SystemSetting_;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;


@DependsOn(DeploymentTransactionSupport.SERVICE_NAME)
@Service(SystemSettings.SERVICE_NAME)
public class SystemSettingsImpl implements SystemSettings {

	private final JpaDao jpaDao;

	private final ApplicationEventPublisher eventPublisher;

	private final AtomicReference<Map<LOV, String>> settings;

	public SystemSettingsImpl(JpaDao jpaDao, ApplicationEventPublisher eventPublisher) {
		this.jpaDao = jpaDao;
		this.eventPublisher = eventPublisher;
		settings = new AtomicReference<>(loadSettings());
		instance.set(this);
	}

	@Override
	public String getValue(LOV key) {
		return settings.get().get(key);
	}

	@Override
	public String getValue(LOV key, String defaultValue) {
		return settings.get().getOrDefault(key, defaultValue);
	}

	public boolean getBooleanValue(LOV key) {
		return BooleanUtils.toBoolean(getValue(key));
	}

	@Override
	public int getIntegerValue(LOV key, int defaultValue) {
		return NumberUtils.toInt(getValue(key), defaultValue);
	}

	@Override
	public long getLongValue(LOV key, long defaultValue) {
		return NumberUtils.toLong(getValue(key), defaultValue);
	}

	@Override
	public List<String> getListValue(LOV key) {
		return Optional.ofNullable(getValue(key))
				.map(s -> s.split(","))
				.map(array -> Arrays.stream(array).map(String::trim)
						.filter(StringUtils::isNotBlank).collect((Collectors.toList()))
				).orElse(new ArrayList<>());
	}

	@Override
	public void reload() {
		// todo
		synchronized (this) {
			Map<LOV, String> current = settings.get();
			Map<LOV, String> pending = loadSettings();
			settings.set(pending);
			CollectionUtils.disjunction(current.entrySet(), pending.entrySet())
					.stream().map(Map.Entry::getKey)
					.distinct().map(lov -> new SystemSettingChangedEvent(lov, this))
					.forEach(eventPublisher::publishEvent);
		}
	}

	private Map<LOV, String> loadSettings() {
		return jpaDao.getList(SystemSetting.class, (root, cq, cb) -> cb.isNotNull(root.get(SystemSetting_.key))).stream()
				.collect(Collectors.toMap(setting -> new LOV(setting.getKey()), SystemSetting::getValue));
	}

	public Stream<? extends Pair<String, String>> select(Predicate<String> predicate) {
		return settings.get().entrySet().stream()
				.filter(e -> predicate.test(e.getKey().getKey()))
				.map(e -> ImmutablePair.of(e.getKey().getKey(), e.getValue()));
	}


}
