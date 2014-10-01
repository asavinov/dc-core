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

	public boolean isKey();
	public boolean isSuper(); 
	
	public ComTable getInput();
	public void setInput(ComTable input);
	
	public ComTable getOutput();
	public void setOutput(ComTable output);

	public void add();
	public void remove();

	public ComColumnData getColumnData();
	public ComColumnDefinition getColumnDefinition();
}
