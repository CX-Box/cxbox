//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.cxbox.core.crudma.bc.impl;

import lombok.Generated;
import org.springframework.core.annotation.AnnotationUtils;

public final class ExternalBcDescription extends BcDescription {
	private final Class<? extends ExternalResponseServiceMarker> serviceClass;

	public ExternalBcDescription(String name, String parentName, Class<? extends ExternalResponseServiceMarker> serviceClass, boolean refresh) {
		super(name, parentName, ((ExternalCrudmaImplementation)AnnotationUtils.findAnnotation(serviceClass, ExternalCrudmaImplementation.class)).value(), refresh);
		this.serviceClass = serviceClass;
	}

	@Generated
	public Class<? extends ExternalResponseServiceMarker> getServiceClass() {
		return this.serviceClass;
	}
}
