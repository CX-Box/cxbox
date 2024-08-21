package org.cxbox.core.external.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


/**
 * Stores PARENT DTO of current request bc.
 * Mote! DOES NOT store dto of current bc
 */

@Getter
@Component
@RequestScope
public class ParentDtoFirstLevelCache<E> {

	private final ConcurrentHashMap<String, Optional<E>> cache = new ConcurrentHashMap<>();

}
