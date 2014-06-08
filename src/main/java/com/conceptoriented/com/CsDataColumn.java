package com.conceptoriented.com;

public interface CsDataColumn { // Work with data

	public CsDataType getDataType();
	
	public int getLength();
	public void setLength(int length);
	
	//
	// Untyped methods
	//
	public boolean isNullValue(int input);
	public Object getValue(int input);
	public void setValue(int input, Object value);
	public void nullifyValues();
	public void appendValue(Object value);
	public void insertValue(int input, Object value);
	
	//
	// Typed methods
	//

}
