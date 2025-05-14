package org.cxbox.model.core.dao.impl;

public enum DialectName {

	ORACLE("Oracle"),
	POSTGRESQL("PostgreSQL");

	private String value;

	DialectName(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
