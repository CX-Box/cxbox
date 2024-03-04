//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.cxbox.core.crudma.bc.impl;

import lombok.Generated;
import org.springframework.core.annotation.AnnotationUtils;

public final class AnySourceBcDescription extends BcDescription {
	private final Class<? extends AnySourceResponseServiceMarker> serviceClass;

	public AnySourceBcDescription(String name, String parentName, Class<? extends AnySourceResponseServiceMarker> serviceClass, boolean refresh) {
		super(name, parentName, ((AnySourceCrudmaImplementation)AnnotationUtils.findAnnotation(serviceClass, AnySourceCrudmaImplementation.class)).value(), refresh);
		this.serviceClass = serviceClass;
	}

	@Generated
	public Class<? extends AnySourceResponseServiceMarker> getServiceClass() {
		return this.serviceClass;
	}
}
