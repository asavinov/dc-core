package com.conceptoriented.com;

public interface CsSchema extends CsTable {
	
	public CsTable getPrimitive(CsDataType dataType);
	public CsTable getRoot();

	public CsTable createTable(String name, CsDataType dataType, CsTable parent);
	
	// Get primitive sets including Root and Top.
	// Maybe define for a set or table?
	// At least, for a set, it is convenient to find its Root and Top (Schema)


}
