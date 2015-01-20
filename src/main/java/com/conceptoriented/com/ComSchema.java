package com.conceptoriented.com;

public interface ComSchema extends ComTable {
	
	public Workspace getWorkspace();
	public void setWorkspace(Workspace workspace);
	
	public ComTable getPrimitive(String dataType);
	public ComTable getRoot();

    // Table factory

	public ComTable createTable(String name);
	public ComTable addTable(ComTable table, ComTable parent, String superName);
	public void deleteTable(ComTable table);
	public void renameTable(ComTable table, String newName);

    // Column factory

	public ComColumn createColumn(String name, ComTable input, ComTable output, boolean isKey);
	public void deleteColumn(ComColumn column);
	public void renameColumn(ComColumn column, String newName);
}

enum ComDataType
{
    // Built-in types in C#: http://msdn.microsoft.com/en-us/library/vstudio/ya5y69ds.aspx
    Void, // Null, Nothing, Empty no value. Can be equivalent to Top.
    Top, // Maybe equivalent to Void
    Bottom, // The most specific type but introduced formally. This guarantees that any set has a lesser set.
    Root, // It is surrogate or reference
    Integer,
    Double,
    Decimal,
    String,
    Boolean,
    DateTime,
    Set, // User-defined. It is any set that is not root (non-primititve type). Arbitrary user-defined name.
}
