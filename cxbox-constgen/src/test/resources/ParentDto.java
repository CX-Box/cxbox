package org.cxbox.constgen;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@GeneratesDtoMetamodel
public class ParentDto {

	private int parentField;

	public int getParentField() {
		return this.parentField;
	}

}
