/*
 * © OOO "SI IKS LAB", 2022-2026
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

package org.cxbox.meta.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.dto.MetaValidationResult;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.service.MetaResourceReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.logging.LogLevel;

@RequiredArgsConstructor
public class MetaValidationService {

	protected static final String LOG_PREFIX = "[META_VALIDATION] ";

	protected static final Logger validationLog = LoggerFactory.getLogger(MetaValidationService.class);

	protected final Map<String, MetaValidator> validators;

	protected final TransactionService txService;

	public List<MetaValidationResult> validate(MetaResourceReaderService reader, LogLevel logLevel) {
		return validateWithoutTransaction(() -> runValidators(
				reader.getScreens(), reader.getViews(), reader.getWidgets().stream()
						.collect(Collectors.toMap(widget -> widget, widget -> JsonUtils.writeValue(widget.getOptions()))), logLevel
		));
	}

	public List<MetaValidationResult> validate(List<ScreenSourceDto> screens, List<ViewSourceDTO> viewDtos,
			Map<WidgetSourceDTO, String> widgets, LogLevel logLevel) {
		return validateWithoutTransaction(() -> runValidators(screens, viewDtos, widgets, logLevel));
	}

	/**
	 * Suspends the caller transaction so validator failures cannot mark login rollback-only.
	 */
	private List<MetaValidationResult> validateWithoutTransaction(
			Invoker<List<MetaValidationResult>, RuntimeException> validation) {
		try {
			return txService.invokeNoTx(validation);
		} catch (Exception e) {
			validationLog.warn(LOG_PREFIX + "Meta validation failed, problems are unknown", e);
			var message = MetaValidationResult.of("Meta validation failed: " + e)
					.setValidatorName("metaValidation")
					.setTitle("Meta validation")
					.setExecutionFailed(true);
			return List.of(message);
		}
	}

	protected List<MetaValidationResult> runValidators(List<ScreenSourceDto> screens, List<ViewSourceDTO> viewDtos,
			Map<WidgetSourceDTO, String> widgets, LogLevel logLevel) {
		var level = toSlf4jLevel(logLevel);
		var messages = new ArrayList<MetaValidationResult>();
		validators.forEach((name, validator) -> messages.addAll(runValidator(
				name,
				validator,
				screens,
				viewDtos,
				widgets,
				level
		)));
		return messages;
	}

	protected List<MetaValidationResult> runValidator(String validatorBeanName, MetaValidator validator,
			List<ScreenSourceDto> screens, List<ViewSourceDTO> viewDtos, Map<WidgetSourceDTO, String> widgets, Level level) {
		var validatorLog = LoggerFactory.getLogger(AopUtils.getTargetClass(validator));
		var messages = new ArrayList<MetaValidationResult>();
		var title = validatorBeanName;
		try {
			var validatorTitle = StringUtils.defaultIfBlank(validator.title(), validatorBeanName);
			title = validatorTitle;
			var problems = Stream.ofNullable(validator.validate(screens, viewDtos, widgets)).flatMap(List::stream)
					.filter(Objects::nonNull)
					.map(problem -> withResolvedViews(problem, viewDtos).setValidatorName(validatorBeanName)
							.setTitle(validatorTitle).setExecutionFailed(false))
					.toList();
			messages.addAll(problems);
			if (level != null) {
				logValidatorReport(validatorLog, level, title, problems);
			}
		} catch (Exception e) {
			// Report validator failures even when problem logging is disabled.
			validatorLog.warn(LOG_PREFIX + "Meta validator {} failed, its problems are unknown", validatorBeanName, e);
			messages.add(MetaValidationResult.of("Meta validator failed: " + e).setValidatorName(validatorBeanName)
					.setTitle(title)
					.setExecutionFailed(true));
		}
		return messages;
	}

	protected Level toSlf4jLevel(LogLevel logLevel) {
		return switch (logLevel) {
			case FATAL, ERROR -> Level.ERROR;
			case WARN -> Level.WARN;
			case INFO -> Level.INFO;
			case DEBUG -> Level.DEBUG;
			case TRACE -> Level.TRACE;
			case OFF -> null;
		};
	}

	protected MetaValidationResult withResolvedViews(MetaValidationResult problem, List<ViewSourceDTO> viewDtos) {
		var viewNames = new LinkedHashSet<>(problem.getViews());
		var widgetNames = new LinkedHashSet<>(problem.getWidgets());
		viewNames.remove(null);
		widgetNames.remove(null);
		if (viewNames.isEmpty()) {
			widgetNames.forEach(widgetName -> viewDtos.stream()
					.filter(view -> view != null && view.getWidgets() != null)
					.filter(view -> view.getWidgets().stream()
							.anyMatch(widget -> widget != null && widgetName.equals(widget.getWidgetName())))
					.map(ViewSourceDTO::getName).filter(Objects::nonNull).forEach(viewNames::add));
		}
		return MetaValidationResult.of(Objects.toString(problem.getMessage(), "")).setViews(viewNames)
				.setWidgets(widgetNames)
				.setServerLogDetails(problem.getServerLogDetails());
	}

	protected void logValidatorReport(Logger validatorLog, Level level, String title,
			List<MetaValidationResult> problems) {
		if (problems.isEmpty()) {
			return;
		}
		var report = new StringBuilder(LOG_PREFIX + title + ": " + problems.size() + " problem(s)");
		appendProblemGroup(
				report, "", problems.stream()
						.filter(problem -> problem.getViews().isEmpty() && problem.getWidgets().isEmpty())
						.toList()
		);
		problems.stream()
				.filter(problem -> !problem.getViews().isEmpty())
				.collect(Collectors.groupingBy(
						problem -> String.join(", ", problem.getViews()) + ":",
						LinkedHashMap::new, Collectors.toList()
				))
				.forEach((views, viewProblems) -> appendProblemGroup(report, views, viewProblems));
		appendProblemGroup(
				report, "not on any view:", problems.stream()
						.filter(problem -> problem.getViews().isEmpty() && !problem.getWidgets().isEmpty())
						.toList()
		);
		problems.stream().map(MetaValidationResult::getServerLogDetails).filter(StringUtils::isNotEmpty).distinct()
				.forEach(details -> report.append("\n").append(details));
		validatorLog.atLevel(level).log(report.toString());
	}

	protected void appendProblemGroup(StringBuilder report, String group, List<MetaValidationResult> problems) {
		var indent = group.isEmpty() ? "" : "  ";
		if (!group.isEmpty() && !problems.isEmpty()) {
			report.append("\n").append(group);
		}
		problems.forEach(problem -> {
			var widgets = String.join(", ", problem.getWidgets());
			var line = (widgets.isEmpty() ? "" : widgets + ": ") + problem.getMessage();
			report.append("\n").append(indent).append("- ").append(line.replace("\n", "\n  " + indent));
		});
	}

}
