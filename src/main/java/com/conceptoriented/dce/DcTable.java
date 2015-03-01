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
public interface DcTable {

    public String getName();
    public void setName(String name);

    public boolean isPrimitive();

    //
    // Outputs
    //
    public List<DcColumn> getColumns();

    public DcColumn getSuperColumn();
    public DcTable getSuperTable();
    public DcSchema getSchema();

    //
    // Inputs
    //
    public List<DcColumn> getInputColumns();

    public List<DcColumn> getSubColumns();
    public List<DcTable> getSubTables();
    public List<DcTable> getAllSubTables();

    //
    // Poset relation
    //

    boolean isSubTable(DcTable parent); // Is subset of the specified table
    boolean isInput(DcTable set); // Is lesser than the specified table
    boolean isLeast(); // Has no inputs
    boolean isGreatest(); // Has no outputs

    //
    // Names
    //
    DcColumn getColumn(String name); // Greater column
    DcTable getTable(String name); // TODO: Greater table/type - not subtable
    DcTable getSubTable(String name); // Subtable

    public DcTableData getData();
    public DcTableDefinition getDefinition();
}

/**
 * Working with data in the table.
 *
 * @author savinov
 *
 */
interface DcTableData {

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

    int find(DcColumn[] dims, Object[] values);
    int append(DcColumn[] dims, Object[] values);
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
interface DcTableDefinition
{
    TableDefinitionType getDefinitionType();
    void setDefinitionType(TableDefinitionType value);

    ExprNode getWhereExpr();
    void setWhereExpr(ExprNode value);

    ExprNode getOrderbyExp();
    void setOrderbyExp(ExprNode value);

    DcIterator getWhereEvaluator();

    void populate();

    void unpopulate(); // Is not it Length=0?

    //
    // Dependencies. The order is important and corresponds to dependency chain
    //
    List<DcTable> usesTables(boolean recursive); // This element depends upon
    List<DcTable> isUsedInTables(boolean recursive); // Dependants

    List<DcColumn> usesColumns(boolean recursive); // This element depends upon
    List<DcColumn> isUsedInColumns(boolean recursive); // Dependants
}

enum TableDefinitionType // Specific types of table formula
{
    FREE, // No definition for the table (and cannot be defined). Example: manually created table with primitive dimensions.
    ANY, // Arbitrary formula without constraints can be provided with a mix of various expression types
    PROJECTION, // Table gets its elements from (unique) outputs of some function
    PRODUCT, // Table contains all combinations of its greater (key) sets satisfying the constraints
    FILTER, // Tables contains a subset of elements from its super-set
}
