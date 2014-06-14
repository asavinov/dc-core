package com.conceptoriented.com;

import java.util.List;

public interface CsTable {

	public String getName();
	public void setName(String name);
	
	public CsDataType getDataType();

	public List<CsColumn> getOutputColumns();
	public CsColumn getSuperColumn();
	public CsSchema getSchema();
	public List<CsColumn> getKeyColumns(); // No super
	public List<CsColumn> getNonkeyColumns();

	public List<CsColumn> getInputColumns();
	public List<CsColumn> getSubColumns();

	public CsColumn getColumn(String name); // Name resolution
	public CsTable getTable(String name); // Find sub-table
	public CsTable findTable(String name); // Find a table among all sub-tables recursively

	public CsTableData getTableData();
}

enum CsDataType
{
    Void, // Nothing, no value. Can be equivalent to Top or Null.
    Top,
    Bottom,
    Root, // It is surrogate or reference
    Integer,
    Double,
    Decimal,
    String,
    Boolean,
    DateTime,
}
