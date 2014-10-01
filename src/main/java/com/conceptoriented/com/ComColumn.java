package com.conceptoriented.com;

/**
 * Describes one column. 
 * 
 * @author savinov
 *
 */
public interface ComColumn {

	public String getName();
	public void setName(String name);

	//
	// Properties
	//
	public boolean isKey();
	void setKey(boolean isKey);

	public boolean isSuper(); 
	void setSuper(boolean isSuper); 

	public boolean isPrimitive(); 
	
	//
	// Input and output
	//
	public ComTable getInput();
	public void setInput(ComTable input);
	
	public ComTable getOutput();
	public void setOutput(ComTable output);

	public void add();
	public void remove();

	//
	// Data and definition objects
	//
	public ComColumnData getData();
	public ComColumnDefinition getDefinition();

}
