package com.conceptoriented.dce;

/**
 * Working with data in the table.
 *
 * @author savinov
 *
 */
public interface DcTableData {

    public int getLength();
    public void setLength(int length);

    public void setAutoIndex(boolean value);

    public boolean isIndexed();

    public void reindex();

    //
    // Value methods (convenience, probably should be removed and replaced by manual access to dimensions)
    //

    Object getValue(String name, int offset);
    void setValue(String name, int offset, Object value);

    //
    // Tuple (flat record) methods: append, insert, remove, read, write.
    //

    int find(DcColumn[] dims, Object[] values);
    int append(DcColumn[] dims, Object[] values);
    void remove(int input);

    //
    // Expression (nested record) methods: append, insert, remove, read, write.
    //

    int find(ExprNode expr);
    boolean canAppend(ExprNode expr);
    int append(ExprNode expr);
}
