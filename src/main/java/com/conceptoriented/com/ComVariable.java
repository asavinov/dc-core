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

    public void nullify();

    //
    // Typed methods
    //
}

class Variable implements ComVariable
{
    protected boolean _isNull;
    Object _value;


    //
    // ComVariable interface
    //

    protected String _name;
	@Override
    public String getName() { return _name; }
	@Override
    public void getName(String name) { _name = name; }

    protected String _typeName;
	@Override
    public String getTypeName() { return _typeName; }
	@Override
    public void setTypeName(String value) { _typeName = value; }

    protected ComTable _typeTable;
	@Override
    public ComTable getTypeTable() { return _typeTable; }
	@Override
    public void setTypeTable(ComTable value) { _typeTable = value; }

	@Override
    public boolean isNull() { return _isNull; }

	@Override
    public Object getValue() {
        return _isNull ? null : _value;
    }

	@Override
    public void setValue(Object value) {
        if (value == null)
        {
            _value = null;
            _isNull = true;
        }
        else
        {
            _value = value;
            _isNull = false;
        }
    }

	@Override
    public void nullify()
    {
        _isNull = true;
    }

    public Variable(String name, String type)
    {
        _name = name;
        _typeName = type;

        _isNull = true;
        _value = null;
    }

    public Variable(String name, ComTable type)
    {
        _name = name;
        _typeName = type.getName();
        _typeTable = type;

        _isNull = true;
        _value = null;
    }
}
