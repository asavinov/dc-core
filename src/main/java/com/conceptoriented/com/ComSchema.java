package com.conceptoriented.com;

public interface ComSchema extends ComTable {
	
	public ComTable getPrimitive(CsDataType dataType);
	public ComTable getRoot();

	public ComTable createTable(String name, CsDataType dataType, ComTable parent);
}
