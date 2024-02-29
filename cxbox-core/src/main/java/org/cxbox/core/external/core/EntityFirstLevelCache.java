package org.cxbox.core.external.core;

import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Component
@RequestScope
public class EntityFirstLevelCache<E> {

	private final ConcurrentHashMap<String, E> cache = new ConcurrentHashMap<>();

}
