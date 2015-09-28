package org.conceptoriented.dc.data;

public class DimDataEmpty implements DcColumnData
{
    protected int _length;
    @Override
    public int getLength() {
        return _length;
    }
    @Override
    public void setLength(int value) {
        _length = value;
    }

    protected boolean _autoIndex;
    @Override
    public boolean isAutoIndex() {
        return _autoIndex;
    }
    @Override
    public void setAutoIndex(boolean value) {
        _autoIndex = value;
    }

    protected boolean _indexed;
    @Override
    public boolean isIndexed() {
        return _indexed;
    }

    @Override
    public void reindex() { }

    @Override
    public boolean isNull(int input) { return true; }

    @Override
    public Object getValue(int input) { return null; }

    @Override
    public void setValue(int input, Object value) { }
    @Override
    public void setValue(Object value) { }

    @Override
    public void nullify() { }

    @Override
    public void append(Object value) { }

    @Override
    public void insert(int input, Object value) { }

    @Override
    public void remove(int input) { }

    @Override
    public Object project(int[] offsets) { return null; }

    @Override
    public int[] deproject(Object value) { return null; } // Or empty array
}
