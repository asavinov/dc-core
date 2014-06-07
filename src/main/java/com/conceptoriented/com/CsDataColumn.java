package com.conceptoriented.com;

public interface CsDataColumn { // Work with data

	public CsDataType getDataType();
	
	public int getLength();
	public void setLength(int length);
	
	//
	// Untyped methods
	//
	boolean IsNullValue(int input);
	Object getValue(int input);
	void setValue(int input, Object value);

	//
	// Typed methods
	//

}
