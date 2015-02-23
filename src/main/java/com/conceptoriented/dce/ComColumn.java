 /*
 * Copyright 2013-2015 Alexandr Savinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.conceptoriented.dce;

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

/**
 * Describes and computes one function in terms of other functions. 
 * 
 * @author savinov
 *
 */
interface ComColumnDefinition {

	public boolean isAppendData();
	public void setAppendData(boolean value);

	public boolean isAppendSchema();
	public void setAppendSchema(boolean value);

	public ColumnDefinitionType getDefinitionType();
	public void setDefinitionType(ColumnDefinitionType columnDefinitionType);
	
    //
    // COEL (language) representation
    //

	public String getFormula();
	public void setFormula(String formula);

    //
    // Structured (object) representation
    //
	
	public ExprNode getFormulaExpr();
	public void setFormulaExpr(ExprNode exprNode);

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
    public void setMeasurePaths(List<DimPath> measurePaths);

    public String getUpdater();
    public void setUpdater(String updater);

    
    //
    // Compute
    //

	public void evaluate();
	
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
