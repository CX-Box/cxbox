package org.cxbox.meta.ui.field;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WidgetTypeFamily {

	INFO("Info"),
	FORM("Form"),
	LIST("List"),
	GROUPING_HIERARCHY("GroupingHierarchy"),
	RELATION_GRAPH("RelationGraph"),
	STATS_BLOCK("StatsBlock"),
	ASSOC_LIST_POPUP("AssocListPopup"),
	PICK_LIST_POPUP("PickListPopup"),
	FORM_POPUP("FormPopup"),
	HEADER_WIDGET("HeaderWidget"),
	LEVEL_MENU("LevelMenu"),
	STEPS("Steps"),
	CALENDAR_LIST("CalendarList"),
	CALENDAR_YEAR_LIST("CalendarYearList"),
	CARD_LIST("CardList"),
	CARD_CAROUSEL_LIST("CardCarouselList");

	private final String value;
}
