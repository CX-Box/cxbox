package org.cxbox.core.dto.rowmeta;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FilterGroupDTOList {

	private final List<FilterGroupDTO> filterGroups;
}
