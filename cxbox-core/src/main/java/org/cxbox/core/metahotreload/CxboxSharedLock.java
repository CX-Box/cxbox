package org.cxbox.core.metahotreload;

public interface CxboxSharedLock {

	void acquireAndExecute(Runnable runnable);

	void waitForLock();

}
