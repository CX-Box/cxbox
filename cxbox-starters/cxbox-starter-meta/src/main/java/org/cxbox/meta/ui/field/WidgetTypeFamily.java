package org.cxbox.meta.ui.field;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WidgetTypeFamily {

	INFO("Info"),
	FROM("From"),
	LIST("List"),
	GROUPING_HIERARCHY("GroupingHierarchy"),
	STATS_BLOCK("StatsBlock"),
	ASSOC_LIST_POPUP("AssocListPopup"),
	PICK_LIST_POPUP("PickListPopup"),
	FORM_POPUP("FormPopup"),
	HEADER_WIDGET("HeaderWidget"),
	LEVEL_MENU("LevelMenu"),
	STEPS("Steps");

	private final String value;
}
