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
 * Describes one table. 
 * 
 * @author savinov
 *
 */
public interface ComTable {

	public String getName();
	public void setName(String name);
	
	public boolean isPrimitive();

    //
    // Outputs
    //
	public List<ComColumn> getColumns();

	public ComColumn getSuperColumn();
	public ComTable getSuperTable();
	public ComSchema getSchema();

    //
    // Inputs
    //
	public List<ComColumn> getInputColumns();

	public List<ComColumn> getSubColumns();
	public List<ComTable> getSubTables();
	public List<ComTable> getAllSubTables();

    //
    // Poset relation
    //

	boolean isSubTable(ComTable parent); // Is subset of the specified table
    boolean isInput(ComTable set); // Is lesser than the specified table
    boolean isLeast(); // Has no inputs
    boolean isGreatest(); // Has no outputs

    //
    // Names
    //
    ComColumn getColumn(String name); // Greater column
    ComTable getTable(String name); // TODO: Greater table/type - not subtable
    ComTable getSubTable(String name); // Subtable

	public ComTableData getData();
	public ComTableDefinition getDefinition();
}

/**
 * Working with data in the table.   
 * 
 * @author savinov
 *
 */
interface ComTableData {

	public int getLength();
	public void setLength(int length);

    //
    // Value methods (convenience, probably should be removed and replaced by manual access to dimensions)
    //

    Object getValue(String name, int offset);
    void setValue(String name, int offset, Object value);

    //
    // Tuple (flat record) methods: append, insert, remove, read, write.
    //

    int find(ComColumn[] dims, Object[] values);
    int append(ComColumn[] dims, Object[] values);
    void remove(int input);

    //
    // Expression (nested record) methods: append, insert, remove, read, write.
    //

    int find(ExprNode expr);
    boolean canAppend(ExprNode expr);
    int append(ExprNode expr);
}

/**
 * Defines data in a table. 
 * 
 * @author savinov
 *
 */
interface ComTableDefinition
{
    TableDefinitionType getDefinitionType();
    void setDefinitionType(TableDefinitionType value);

    ExprNode getWhereExpr();
    void setWhereExpr(ExprNode value);

    ExprNode getOrderbyExp();
    void setOrderbyExp(ExprNode value);

    ComEvaluator getWhereEvaluator();

    void populate();

    void unpopulate(); // Is not it Length=0?

    //
    // Dependencies. The order is important and corresponds to dependency chain
    //
    List<ComTable> usesTables(boolean recursive); // This element depends upon
    List<ComTable> isUsedInTables(boolean recursive); // Dependants

    List<ComColumn> usesColumns(boolean recursive); // This element depends upon
    List<ComColumn> isUsedInColumns(boolean recursive); // Dependants
}

enum TableDefinitionType // Specific types of table formula
{
    FREE, // No definition for the table (and cannot be defined). Example: manually created table with primitive dimensions.
    ANY, // Arbitrary formula without constraints can be provided with a mix of various expression types
    PROJECTION, // Table gets its elements from (unique) outputs of some function
    PRODUCT, // Table contains all combinations of its greater (key) sets satsifying the constraints
    FILTER, // Tables contains a subset of elements from its super-set
}
