package com.conceptoriented.com;

public interface CsSchema extends CsTable {
	
	public CsTable getPrimitive(CsDataType dataType);
	public CsTable getRoot();

	public CsTable createTable(String name, CsDataType dataType, CsTable parent);
}
