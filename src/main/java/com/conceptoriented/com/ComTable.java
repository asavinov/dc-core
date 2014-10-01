package com.conceptoriented.com;

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
	public List<ComColumn> getSubTables();
	public List<ComColumn> getAllSubTables();

    //
    // Poset relation
    //

	boolean IsSubTable(ComTable parent); // Is subset of the specified table
    boolean IsInput(ComTable set); // Is lesser than the specified table
    boolean IsLeast(); // Has no inputs
    boolean IsGreatest(); // Has no outputs

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

    Object GetValue(String name, int offset);
    void SetValue(String name, int offset, Object value);

    //
    // Tuple methods: append, insert, remove, read, write.
    //

    int Find(ComColumn[] dims, Object[] values);
    int Append(ComColumn[] dims, Object[] values);
    void Remove(int input);

    int Find(ExprNode expr);
    boolean CanAppend(ExprNode expr);
    int Append(ExprNode expr);
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
    void getWhereExpr(ExprNode value);

    ExprNode getOrderbyExp();
    void getOrderbyExp(ExprNode value);

    ComEvaluator getWhereEvaluator();

    void Populate();

    void Unpopulate(); // Is not it Length=0?

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
    NONE, // No definition for the table (and cannot be defined). Example: manually created table with primitive dimensions.
    ANY, // Arbitrary formula without constraints can be provided with a mix of various expression types
    PROJECTION, // Table gets its elements from (unique) outputs of some function
    PRODUCT, // Table contains all combinations of its greater (key) sets satsifying the constraints
    FILTER, // Tables contains a subset of elements from its super-set
}
