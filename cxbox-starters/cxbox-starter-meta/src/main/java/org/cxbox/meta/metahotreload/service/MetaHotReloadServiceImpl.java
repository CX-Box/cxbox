package org.cxbox.meta.metahotreload.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.MetaHotReloadService;
import org.cxbox.api.data.dictionary.InternalRole;
import org.cxbox.api.service.session.InternalAuthorizationService;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.dictionary.DictionaryProvider;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.entity.Responsibilities;
import org.cxbox.meta.entity.Responsibilities.ResponsibilityType;
import org.cxbox.meta.entity.ResponsibilitiesAction;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;

@Slf4j
@RequiredArgsConstructor
public class MetaHotReloadServiceImpl implements MetaHotReloadService {

	protected final MetaConfigurationProperties config;

	protected final MetaResourceReaderService metaResourceReaderService;

	protected final InternalAuthorizationService authzService;

	protected final TransactionService txService;

	protected final MetaRepository metaRepository;

	private final Optional<DictionaryProvider> dictionaryProvider;

	private static Stream<ViewSourceDTO> getViewsByWidget(List<ViewSourceDTO> viewDtos, WidgetSourceDTO widget) {
		return viewDtos
				.stream()
				.filter(view -> view.getWidgets().stream()
						.anyMatch(vw -> Objects.equals(vw.getWidgetName(), widget.getName())));
	}

	private static Stream<String> parseIncludeObject(Map actionOrGroupProps) {
		Object groupIncludeProp = actionOrGroupProps.getOrDefault(WidgetSourceDTO.INCLUDE_PROP, null);
		List<String> groupIncludes = new ArrayList<>();
		if (groupIncludeProp == null) {
			Object groupNameProp = actionOrGroupProps.getOrDefault("type", null);
			if (groupNameProp instanceof String groupNamePropStr) {
				groupIncludes.add(groupNamePropStr);
			}
		} else if (groupIncludeProp instanceof List groupIncludeList) {
			groupIncludeList.forEach(groupInclude -> {
				if (groupInclude instanceof String groupIncludeStr) {
					groupIncludes.add(groupIncludeStr);
				} else {
					log.warn("Not supported format for migration, so entry "
							+ JsonUtils.writeValue(groupInclude) + " will be ignored");
				}
			});
		} else {
			log.warn("Not supported format for migration, so entry "
					+ JsonUtils.writeValue(groupIncludeProp) + " will be ignored");
		}
		return groupIncludes.stream();
	}

	public void loadMeta() {
		List<ScreenSourceDto> screenDtos = metaResourceReaderService.getScreens();
		List<ViewSourceDTO> viewDtos = metaResourceReaderService.getViews();
		Map<WidgetSourceDTO, String> widgetDtos = metaResourceReaderService.getWidgets().stream()
				.collect(Collectors.toMap(w -> w, w -> JsonUtils.writeValue(w.getOptions())));

		authzService.loginAs(authzService.createAuthentication(InternalAuthorizationService.VANILLA));

		txService.invokeInTx(() -> {
			metaRepository.deleteAllMeta();
			responsibilitiesActionProcess(widgetDtos, viewDtos);
			responsibilitiesProcess(screenDtos, viewDtos);
			loadMetaAfterProcess();
			return null;
		});
	}

	public void responsibilitiesProcess(List<ScreenSourceDto> screenDtos, List<ViewSourceDTO> viewDtos) {
		if (config.isViewAllowedRolesEnabled()) {
			Map<String, String> viewToScreenMap = new HashMap<>();
			metaRepository.getAllScreens()
					.forEach((screenName, screenDto) -> ((ScreenDTO) screenDto.getMeta()).getViews().stream()
							.map(ViewDTO::getName)
							.forEach(viewName -> viewToScreenMap.put(viewName, screenName)));

			List<Responsibilities> responsibilities = new ArrayList<>();
			viewDtos.forEach(view -> {
				view.getRolesAllowed().forEach(role -> {
					responsibilities.add(new Responsibilities()
							.setResponsibilityType(ResponsibilityType.VIEW)
							.setInternalRoleCD(role)
							.setView(view.getName()));
				});
			});
			metaRepository.deleteAndSaveResponsibilities(responsibilities);
		}
	}

	private void responsibilitiesActionProcess(Map<WidgetSourceDTO, String> widgetDtos, List<ViewSourceDTO> viewDtos) {
		if (config.isWidgetActionGroupsEnabled()) {
			var widgetsToInclude = extractWidgetActionGroupsInclude(widgetDtos);
			List<ResponsibilitiesAction> responsibilitiesActions = new ArrayList<>();
			if (config.isWidgetActionGroupsCompact()) {
				widgetsToInclude
						.forEach((widget, actionList) -> actionList
								.forEach(action -> responsibilitiesActions
										.add(new ResponsibilitiesAction()
												.setInternalRoleCD(ResponsibilitiesAction.ANY_INTERNAL_ROLE_CD)
												.setAction(action)
												.setView(ResponsibilitiesAction.ANY_VIEW)
												.setWidget(widget.getName())
										)));
			} else {
				var roles = getInternalRoles().orElse(List.of(ResponsibilitiesAction.ANY_INTERNAL_ROLE_CD));
				widgetsToInclude
						.forEach((widget, actionList) -> actionList
								.forEach(action -> getViewsByWidget(viewDtos, widget)
										.forEach(view -> roles
												.forEach(role -> responsibilitiesActions
														.add(new ResponsibilitiesAction()
																.setInternalRoleCD(role)
																.setAction(action)
																.setView(view.getName())
																.setWidget(widget.getName())
														)))));
			}
			metaRepository.deleteAndSaveResponsibilitiesAction(responsibilitiesActions);
			log.info(widgetsToInclude.toString());
		}
	}

	private Map<WidgetSourceDTO, List<String>> extractWidgetActionGroupsInclude(Map<WidgetSourceDTO, String> widgetDtos) {
		return widgetDtos.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getKey,
						entry -> Optional.ofNullable(JsonUtils.readValue(WidgetSourceDTO.Options.class, entry.getValue()))
								.map(WidgetSourceDTO.Options::getActionGroups)
								.map(WidgetSourceDTO.ActionGroupsDTO::getInclude)
								.map(inc -> inc.stream()
										.flatMap(actionOrGroup -> {
											if (actionOrGroup instanceof String str) {
												return Stream.<String>of(str);
											} else if (actionOrGroup instanceof Map actionOrGroupProps) {
												return parseIncludeObject(actionOrGroupProps);
											} else {
												log.warn("Not supported format for migration, so entry "
														+ JsonUtils.writeValue(actionOrGroup) + " will be ignored");
												return null;
											}
										})
										.filter(Objects::nonNull)
										.toList()
								)
								.orElse(new ArrayList<>())
				));
	}

	private Optional<List<String>> getInternalRoles() {
		return dictionaryProvider
				.map(dp -> dp.getAll(InternalRole.class).stream()
						.map(InternalRole::key)
						.toList());
	}

	protected void loadMetaAfterProcess() {

	}

}
