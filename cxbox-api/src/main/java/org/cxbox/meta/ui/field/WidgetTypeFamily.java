package org.cxbox.meta.ui.field;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WidgetTypeFamily {

	INFO("Info"),
	FORM("Form"),
	LIST("List"),
	GROUPING_HIERARCHY("GroupingHierarchy"),
	STATS_BLOCK("StatsBlock"),
	ASSOC_LIST_POPUP("AssocListPopup"),
	PICK_LIST_POPUP("PickListPopup"),
	FORM_POPUP("FormPopup"),
	HEADER_WIDGET("HeaderWidget"),
	LEVEL_MENU("LevelMenu"),
	STEPS("Steps"),
	CALENDAR_LIST("CalendarList"),
	CALENDAR_YEAR_LIST("CalendarYearList"),

	SECOND_LEVEL_MENU("SecondLevelMenu"),
	THIRD_LEVEL_MENU("ThirdLevelMenu"),
	FOURTH_LEVEL_MENU("FourthLevelMenu"),
	RING_PROGRESS("RingProgress"),
	ADDITIONAL_INFO("AdditionalInfo"),
	ADDITIONAL_LIST("AdditionalList"),
	PIE1_D("Pie1D"),
	COLUMN2_D("Column2D"),
	LINE2_D("Line2D"),
	DUAL_AXES2_D("DualAxes2D"),
	FILE_PREVIEW("FilePreview"),
	CARD_LIST("CardList"),
	CARD_CAROUSEL_LIST("CardCarouselList")

	;

	private final String value;

	private static final Map<String, WidgetTypeFamily> BY_VALUE =
			Arrays.stream(values())
					.collect(Collectors.toUnmodifiableMap(
							WidgetTypeFamily::getValue,
							Function.identity()
					));

	public static Optional<WidgetTypeFamily> fromValue(String value) {
		return Optional.ofNullable(BY_VALUE.get(value));
	}
}
