package org.conceptoriented.dc.data;

public interface DcTableReader {
    public void open();
    public void close();

    public Object next(); // Null if no more elements
}
