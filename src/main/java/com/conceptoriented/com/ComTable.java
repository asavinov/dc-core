package com.conceptoriented.com;

import java.util.List;

public interface ComTable {

	public String getName();
	public void setName(String name);
	
	public CsDataType getDataType();

	public List<ComColumn> getOutputColumns();
	public ComColumn getSuperColumn();
	public ComSchema getSchema();
	public List<ComColumn> getKeyColumns(); // No super
	public List<ComColumn> getNonkeyColumns();

	public List<ComColumn> getInputColumns();
	public List<ComColumn> getSubColumns();

	public ComColumn getColumn(String name); // Name resolution
	public ComTable getTable(String name); // Find sub-table
	public ComTable findTable(String name); // Find a table among all sub-tables recursively

	public ComTableData getTableData();
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
