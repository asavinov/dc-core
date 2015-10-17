package org.conceptoriented.dc.data;

import org.conceptoriented.dc.schema.DcColumn;

public interface DcTableWriter {
	public void open();
    public void close();

    public int find(ExprNode expr);
    public boolean canAppend(ExprNode expr);
    public int append(ExprNode expr);

    //
    // Tuple (flat record) methods: append, insert, remove, read, write.
    //

    int find(DcColumn[] dims, Object[] values);
    int append(DcColumn[] dims, Object[] values);
    void remove(int input);
}
