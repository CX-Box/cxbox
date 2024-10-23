package org.cxbox.core.crudma;

import org.cxbox.core.crudma.bc.BusinessComponent;

public interface PlatformRequest {

	CrudmaActionType getCrudmaActionType();

	BusinessComponent getBc();

}
