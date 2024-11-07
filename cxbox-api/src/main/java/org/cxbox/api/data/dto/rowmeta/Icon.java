package org.cxbox.api.data.dto.rowmeta;


/**
 * Since 4.0.0-M11
 * use to add for icons
 * <pre>{@code
 * @RequiredArgsConstructor
 * @Getter
 * public enum IconsEnum implements Icon {
 *     ARROW_UP("arrow-up #0cbfe9"),
 *     WATERMELON("watermelon"),
 *     DOWN("down");
 *
 *     private final String icon;
 *
 * }
 * </pre>
 */
public interface Icon {

	String getIcon();

}
