package org.conceptoriented.dc.data;

import org.conceptoriented.dc.data.eval.*;

public interface DcTableWriter {
	public void open();
    public void close();

    public int find(ExprNode expr);
    public boolean canAppend(ExprNode expr);
    public int append(ExprNode expr);
}
