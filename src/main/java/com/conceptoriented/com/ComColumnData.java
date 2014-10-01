package com.conceptoriented.com;

/**
 * Storage methods for working with function data like reading and writing function output values for the specified inputs.   
 * 
 * @author savinov
 *
 */
public interface ComColumnData {

	public int getLength();
	public void setLength(int length);
	
    //
    // Untyped methods. Default conversion will be done according to the function type.
    //
	public boolean isNullValue(int input);

	public Object getValue(int input);
	public void setValue(int input, Object value);

	public void nullifyValues();

	public void appendValue(Object value);

	public void insertValue(int input, Object value);
	
	public void Remove(int input);

    //
    // Project/de-project
    //

    Object projectValues(int[] offsets);
    int[] deprojectValue(Object value);

}
