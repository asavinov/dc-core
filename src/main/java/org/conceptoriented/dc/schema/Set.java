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

package org.conceptoriented.dc.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.conceptoriented.dc.data.DcTableData;
import org.conceptoriented.dc.data.DcTableDefinition;
import org.conceptoriented.dc.data.DcTableReader;
import org.conceptoriented.dc.data.DcTableWriter;
import org.conceptoriented.dc.data.DcVariable;
import org.conceptoriented.dc.data.ExprNode;
import org.conceptoriented.dc.data.TableDefinitionType;
import org.conceptoriented.dc.data.TableReader;
import org.conceptoriented.dc.data.TableWriter;
import org.conceptoriented.dc.data.Variable;
import org.conceptoriented.dc.data.query.ExprBuilder;
import org.conceptoriented.dc.schema.*;

public class Set implements DcTable, DcTableData, DcTableDefinition {

    //
    // DcTable interface
    //

    protected String name;
    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isPrimitive() {
        return getSuperTable() instanceof DcSchema; // If its super-set is Top
    }

    // Outputs

    protected List<DcColumn> greaterDims;
    @Override
    public List<DcColumn> getColumns() {
        return greaterDims;
    }
    @Override
    public DcColumn getSuperColumn() {
        Optional<DcColumn> ret = greaterDims.stream().filter(x -> x.isSuper()).findAny();
        return ret.isPresent() ? ret.get() : null;
    }
    @Override
    public DcTable getSuperTable() {
        return getSuperColumn() != null ? getSuperColumn().getOutput() : null;
    }
    @Override
    public DcSchema getSchema() {
        DcTable t = this;
        while(t.getSuperColumn() != null) t = t.getSuperColumn().getOutput();
        return (DcSchema)t;
    }

    // Inputs

    protected List<DcColumn> lesserDims;
    @Override
    public List<DcColumn> getInputColumns() {
        return lesserDims;
    }
    @Override
    public List<DcColumn> getSubColumns() {
        return lesserDims.stream().filter(x -> x.isSuper()).collect(Collectors.toList());
    }
    @Override
    public List<DcTable> getSubTables() {
        return getSubColumns().stream().map(x -> x.getInput()).collect(Collectors.toList());
    }
    @Override
    public List<DcTable> getAllSubTables() {
        List<DcTable> result = new ArrayList<DcTable>();
        result.addAll(getSubTables());
        int count = result.size();
        for (int i = 0; i < count; i++)
        {
            List<DcTable> subsets = result.get(i).getAllSubTables();
            if (subsets == null || subsets.size() == 0)
            {
                continue;
            }
            result.addAll(subsets);
        }

        return result;
    }

    // Poset relation

    @Override
    public boolean isSubTable(DcTable parent) { // Is subset of the specified table
        for (DcTable set = this; set != null; set = set.getSuperTable())
        {
            if (set == parent) return true;
        }
        return false;
    }
    @Override
    public boolean isInput(DcTable set) { // Is lesser than the specified table
        throw new UnsupportedOperationException("TODO");
    }
    @Override
    public boolean isLeast() { // Has no inputs
        return getInputColumns().stream().filter(x -> x.getInput().getSchema() == x.getOutput().getSchema()).count() == 0;
    }
    @Override
    public boolean isGreatest() { // Has no outputs
        return isPrimitive() || getColumns().stream().filter(x -> x.getInput().getSchema() == x.getOutput().getSchema()).count() == 0;
    }


    // Name methods

    @Override
    public DcColumn getColumn(String name) { // Greater column
        Optional<DcColumn> ret = greaterDims.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
        return ret.isPresent() ? ret.get() : null;
    }
    @Override
    public DcTable getTable(String name) { // TODO: Greater table/type - not subtable
        Optional<DcColumn> ret = lesserDims.stream().filter(x -> x.isSuper() && x.getInput().getName().equalsIgnoreCase(name)).findAny();
        return ret.isPresent() ? ret.get().getInput() : null;
    }
    @Override
    public DcTable getSubTable(String name) { // Subtable
        if(getName().equalsIgnoreCase(name)) return this;

        for(DcColumn c : getSubColumns()) {
            DcTable t = c.getInput().getSubTable(name);
            if(t != null) return t;
        }
        return null;
    }

    @Override
    public DcTableData getData() {
        return this;
    }

    //
    // DcTableData
    //

    protected int _length;
    @Override
    public int getLength() {
        return _length;
    }
    @Override
    public void setLength(int value) {
        _length = value;
        for (DcColumn col : getColumns())
        {
            col.getData().setLength(value);
        }
    }

    @Override
    public void setAutoIndex(boolean value) {
        for(DcColumn column : getColumns()) {
            column.getData().setAutoIndex(value);
        }
    }

    @Override
    public boolean isIndexed() {
        for(DcColumn column : getColumns()) {
            if(!column.getData().isIndexed()) return false;
        }
        return true;
    }

    @Override
    public void reindex() {
        for(DcColumn column : getColumns()) {
            column.getData().reindex();
        }
    }

    @Override
    public DcTableReader getTableReader() {
        return new TableReader(this);
    }

    @Override
    public DcTableWriter getTableWriter() {
        return new TableWriter(this);
    }

    @Override
    public DcTableDefinition getDefinition() {
        return this;
    }

    //
    // DcTableDefinition
    //

    @Override
    public TableDefinitionType getDefinitionType() 
    { 
        if (isPrimitive()) return TableDefinitionType.FREE;

        // Try to find incoming generating (append) columns. If they exist then table instances are populated as this dimension output tuples.
        List<DcColumn> inColumns = getInputColumns().stream().filter(d -> d.getData().getDefinition().isAppendData()).collect(Collectors.toList());
        if(inColumns != null && inColumns.size() > 0)
        {
            return TableDefinitionType.PROJECTION;
        }

        // Try to find outgoing key non-primitive columns. If they exist then table instances are populated as their combinations.
        List<DcColumn> outColumns = getColumns().stream().filter(d -> d.isKey() && !d.isPrimitive()).collect(Collectors.toList());
        if(outColumns != null && outColumns.size() > 0)
        {
            return TableDefinitionType.PRODUCT;
        }

        // No instances can be created automatically. 
        return TableDefinitionType.FREE;
	}

    protected String _whereFormula;
    @Override
    public String getWhereFormula() { return _whereFormula; }
    @Override
    public void setWhereFormula(String value) 
    { 
    	_whereFormula = value;
		
        if (Utils.isNullOrEmpty(value)) return;

        ExprBuilder exprBuilder = new ExprBuilder();
        ExprNode expr = exprBuilder.build(_whereFormula);

        setWhereExpr(expr);
    }

    protected ExprNode _whereExpr;
    @Override
    public ExprNode getWhereExpr() { return _whereExpr; }
    @Override
    public void setWhereExpr(ExprNode value) { _whereExpr = value; }

    protected String _orderbyFormula;
    @Override
    public String getOrderbyFormula() { return _orderbyFormula; }
    @Override
    public void setOrderbyFormula(String value) { _orderbyFormula = value; }

    @Override
    public void populate() {
        if (getDefinitionType() == TableDefinitionType.FREE)
        {
            return; // Nothing to do
        }

        setLength(0);

        if (getDefinitionType() == TableDefinitionType.PROJECTION) // There are import dimensions so copy data from another set (projection of another set)
        {
            List<DcColumn> inColumns = getInputColumns().stream().filter(d -> d.getData().getDefinition().isAppendData()).collect(Collectors.toList());

            for(DcColumn inColumn : inColumns) 
            {
                inColumn.getData().getDefinition().evaluate(); // Delegate to column evaluation - it will add records from column expression
            }
        }
        else if (getDefinitionType() == TableDefinitionType.PRODUCT) // Product of local sets (no project/de-project from another set)
        {
            // Input variable for where formula
        	DcVariable thisVariable = new Variable(this.getSchema().getName(), this.getName(), "this");
            thisVariable.setTypeSchema(this.getSchema());
            thisVariable.setTypeTable(this);

            // Evaluator expression for where formula
            ExprNode outputExpr = this.getDefinition().getWhereExpr();
            if(outputExpr != null)
            {
                outputExpr.getOutputVariable().setSchemaName(this.getSchema().getName());
                outputExpr.getOutputVariable().setTypeName("Boolean");
                outputExpr.getOutputVariable().setTypeSchema(this.getSchema());
                outputExpr.getOutputVariable().setTypeTable(this.getSchema().getPrimitive("Boolean"));
                outputExpr.resolve(this.getSchema().getWorkspace(), Arrays.asList(thisVariable));

                outputExpr.evaluateBegin();
            }
        	
            DcTableWriter tableWriter = this.getTableWriter();
            tableWriter.open();

            //
            // Find all local greater dimensions to be varied (including the super-dim)
            //
            DcColumn[] dims = getColumns().stream().filter(x -> x.isKey()).collect(Collectors.toList()).toArray(new DcColumn[0]);
            int dimCount = dims.length; // Dimensionality - how many free dimensions
            Object[] vals = new Object[dimCount]; // A record with values for each free dimension being varied

            //
            // The current state of the search procedure
            //
            int[] lengths = new int[dimCount]; // Size of each dimension being varied (how many offsets in each dimension)
            for (int i = 0; i < dimCount; i++) lengths[i] = dims[i].getOutput().getData().getLength();

            int[] offsets = new int[dimCount]; // The current point/offset for each dimensions during search
            for (int i = 0; i < dimCount; i++) offsets[i] = -1;

            int top = -1; // The current level/top where we change the offset. Depth of recursion.
            do ++top; while (top < dimCount && lengths[top] == 0);

            // Alternative recursive iteration: http://stackoverflow.com/questions/13655299/c-sharp-most-efficient-way-to-iterate-through-multiple-arrays-list
            while (top >= 0)
            {
                if (top == dimCount) // New element is ready. Process it.
                {
                    // Initialize a record and append it
                    for (int i = 0; i < dimCount; i++)
                    {
                        vals[i] = offsets[i];
                    }
                    int input = tableWriter.append(dims, vals);

                    //
                    // Now check if this appended element satisfies the where expression and if not then remove it
                    //
                    if (outputExpr != null)
                    {
                        // Set 'this' variable to the last elements (that has been just appended) which will be read by the expression
                        thisVariable.setValue(this.getData().getLength() - 1);

                        // Evaluate expression
                        outputExpr.evaluate();

                        boolean satisfies = (boolean)outputExpr.getOutputVariable().getValue();

                        if (!satisfies)
                        {
                            setLength(getLength() - 1); // Remove elements
                        }
                    }

                    top--;
                    while (top >= 0 && lengths[top] == 0) // Go up by skipping empty dimensions and reseting
                    { offsets[top--] = -1; }
                }
                else
                {
                    // Find the next valid offset
                    offsets[top]++;

                    if (offsets[top] < lengths[top]) // Offset chosen
                    {
                        do ++top;
                        while (top < dimCount && lengths[top] == 0); // Go up (forward) by skipping empty dimensions
                    }
                    else // Level is finished. Go back.
                    {
                        do { offsets[top--] = -1; }
                        while (top >= 0 && lengths[top] == 0); // Go down (backward) by skipping empty dimensions and reseting
                    }
                }
            }

            if(tableWriter != null)
            {
                tableWriter.close();
            }
            if (outputExpr != null)
            {
                outputExpr.evaluateEnd();
            }
        }
        else
        {
            throw new UnsupportedOperationException("This table definition type is not implemented and cannot be populated.");
        }

    }

    @Override
    public void unpopulate() {
        throw new UnsupportedOperationException();
    }

    // Dependencies. The order is important and corresponds to dependency chain

    @Override
    public List<DcTable> usesTables(boolean recursive) { // This element depends upon
        throw new UnsupportedOperationException();
    }
    @Override
    public List<DcTable> isUsedInTables(boolean recursive) { // Dependants
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DcColumn> usesColumns(boolean recursive) { // This element depends upon
        throw new UnsupportedOperationException();
    }
    @Override
    public List<DcColumn> isUsedInColumns(boolean recursive) { // Dependants
        throw new UnsupportedOperationException();
    }

    //
    // Constructors
    //

    public Set() {
        this("");
    }

    public Set(String name)
    {
        this.name = name;

        greaterDims = new ArrayList<DcColumn>();
        lesserDims = new ArrayList<DcColumn>();
    }

}
