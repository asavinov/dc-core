package com.conceptoriented.com;

public interface ComVariable {

    //
    // Variable name (strictly speaking, it should belong to a different interface)
    //

    public String getName();
    public void getName(String name);

    //
    // Type info
    //

    public String getTypeName();
    public void setTypeName(String typeName);

    public ComTable getTypeTable();
    public void setTypeTable(ComTable typeTable);

    //
    // Variable data. Analogous to the column data interface but without input argument
    //

    public boolean isNull();

    public Object getValue();
    public void setValue(Object value);

    public void NullifyValue();

    //
    // Typed methods
    //

}
