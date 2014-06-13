package com.conceptoriented.com;

/**
 * Describes one column. 
 * 
 * @author savinov
 *
 */
public interface CsColumn {

	public String getName();
	public void setName(String name);

	public boolean isSuper(); 
	public boolean isKey();
	
	public CsTable getInput();
	public void setInput(CsTable input);
	
	public CsTable getOutput();
	public void setOutput(CsTable output);

	public void add();
	public void remove();

	public CsColumnData getColumnData();
	public CsColumnDefinition getColumnDefinition();
}
