package org.conceptoriented.dc.data;

import org.conceptoriented.dc.schema.*;

public class TableReader implements DcTableReader {

    DcTable table;
    int rowid = -1;

    @Override
	public void open() {
    	rowid = -1;
	}

	@Override
	public void close() {
		rowid = table.getData().getLength();
	}

	@Override
	public Object next() {
        if (rowid < table.getData().getLength()-1)
        {
            rowid++;
            return rowid;
        }
        else
        {
            return null;
        }
	}

    public TableReader(DcTable table)
    {
        this.table = table;
    }
}
