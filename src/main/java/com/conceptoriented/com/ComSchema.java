package com.conceptoriented.com;

public interface ComSchema extends ComTable {
	
	public ComTable getPrimitive(ComDataType dataType);
	public ComTable getRoot();

	public ComTable createTable(String name, ComDataType dataType, ComTable parent);
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
