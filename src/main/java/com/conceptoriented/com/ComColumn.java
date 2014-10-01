package com.conceptoriented.com;

import java.util.List;

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

/**
 * Storage methods for working with function data like reading and writing function output values for the specified inputs.   
 * 
 * @author savinov
 *
 */
interface ComColumnData {

	public int getLength();
	public void setLength(int length);
	
    //
    // Untyped methods. Default conversion will be done according to the function type.
    //
	public boolean isNull(int input);

	public Object getValue(int input);
	public void setValue(int input, Object value);

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

/**
 * Describes and computes one function in terms of other functions. 
 * 
 * @author savinov
 *
 */
interface ComColumnDefinition {

	public boolean isGenerating();
	public void setGenerating(boolean isGenerating);

	public ColumnDefinitionType getDefinitionType();
	public void setDefinitionType(ColumnDefinitionType columnDefinitionType);
	
	public ExprNode getFormula();
	public void setFormula(ExprNode exprNode);

	public Mapping getMapping();
	public void setMapping(Mapping mapping);
	
	public ExprNode getWhereExpr();
	public void setWhereExpr(ExprNode exprNode);
	
    //
    // Aggregation
    //

	public ComTable getFactTable(); 
	public void setFactTable(ComTable table); 

    public List<DimPath> getGroupPaths();
    public void setGroupPaths(List<DimPath> groupPaths);

    public List<DimPath> getMeasurePaths();
    public void getMeasurePaths(List<DimPath> measurePaths);

    public String getUpdater();
    public void setUpdater(String updater);

    
    //
    // Compute
    //

	public ComEvaluator getColumnEvaluator(); // Get an object which is used to compute the function values according to the formula
    
	public void initialize();
	public void evaluate();
	public void finish();
	
    //
    // Dependencies. The order is important and corresponds to dependency chain
    //

    List<ComTable> usesTables(boolean recursive); // This element depends upon
    List<ComTable> isUsedInTables(boolean recursive); // Dependants

    List<ComColumn> usesColumns(boolean recursive); // This element depends upon
    List<ComColumn> isUsedInColumns(boolean recursive); // Dependants
    
}

enum ColumnDefinitionType // Specific types of column formula
{
    FREE, // No definition for the column (and cannot be defined). Example: key columns of a product table
    ANY, // Arbitrary formula without constraints which can mix many other types of expressions
    ARITHMETIC, // Column uses only other columns or paths of this same table as well as operations
    LINK, // Column is defined via a mapping represented as a tuple with paths as leaves
    AGGREGATION, // Column is defined via an updater (accumulator) function which is fed by facts using grouping and measure paths
    CASE,
}
