package com.conceptoriented.dce;

/**
 * Storage methods for working with function data like reading and writing function output values for the specified inputs.
 *
 * @author savinov
 *
 */
public interface DcColumnData {

    public int getLength();
    public void setLength(int length);

    //
    // Untyped methods. Default conversion will be done according to the function type.
    //
    public boolean isNull(int input);

    public Object getValue(int input);
    public void setValue(int input, Object value);
    public void setValue(Object value);

    public void nullify();

    public void append(Object value);

    public void insert(int input, Object value);

    public void remove(int input);

    //
    // Project/de-project
    //

    Object project(int[] offsets);
    int[] deproject(Object value);

}
