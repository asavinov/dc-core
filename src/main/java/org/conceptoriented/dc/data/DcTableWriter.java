package org.conceptoriented.dc.data;

public interface DcTableWriter {
	public void open();
    public void close();

    public int find(ExprNode expr);
    public boolean canAppend(ExprNode expr);
    public int append(ExprNode expr);
}
