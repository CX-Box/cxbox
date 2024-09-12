package org.cxbox.meta.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface WidgetName {

	/**
	 * @return widget type prefix, by default includes all prefixes of widget types
	 */
	WidgetTypeFamily[] typeFamily() default {};

}
