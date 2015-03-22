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

package com.conceptoriented.dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.conceptoriented.dc.api.*;

public class Set implements DcTable, DcTableData, DcTableDefinition {

    //
    // ComTable interface
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

    @Override
    public DcTableDefinition getDefinition() {
        return this;
    }

    //
    // ComTableData
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

    // Value methods

    @Override
    public Object getValue(String name, int offset) {
        DcColumn col = getColumn(name);
        return col.getData().getValue(offset);
    }
    @Override
    public void setValue(String name, int offset, Object value) {
        DcColumn col = getColumn(name);
        col.getData().setValue(offset, value);
    }

    // Tuple methods

    @Override
    public int find(DcColumn[] dims, Object[] values) {
        int[] result = java.util.stream.IntStream.range(0, getLength()).toArray(); // All elements of this set (can be quite long)

        boolean hasBeenRestricted = false; // For the case where the Length==1, and no key columns are really provided, so we get at the end result.Length==1 which is misleading. Also, this fixes the problem of having no key dimensions.
        for (int i = 0; i < dims.length; i++)
        {
            hasBeenRestricted = true;
            int[] range = dims[i].getData().deproject(values[i]); // Deproject one value
            result = Utils.intersect(result, range);
            // OPTIMIZE: Write our own implementation for various operations (intersection etc.). Use the fact that they are ordered.
            // OPTIMIZE: Use statistics for column distribution to choose best order of de-projections. Alternatively, the order of dimensions can be set by the external procedure taking into account statistics. Say, there could be a special utility method like SortDimensionsAccordingDiscriminationFactor or SortDimsForFinding tuples.
            // OPTIMIZE: Remember the position for the case this value will have to be inserted so we do not have again search for this positin during insertion. Maybe store it in a static field as part of last operation.

            if (result.length == 0) break; // Not found
        }

        if (result.length == 0) // Not found
        {
            return -1;
        }
        else if (result.length == 1) // Found single element - return its offset
        {
            if (hasBeenRestricted) return result[0];
            else return -result.length;
        }
        else // Many elements satisfy these properties (non-unique identities). Use other methods for getting these records (like de-projection)
        {
            return -result.length;
        }
    }
    @Override
    public int append(DcColumn[] dims, Object[] values) {
        for (int i = 0; i < dims.length; i++)
        {
            dims[i].getData().append(values[i]);
        }

        _length++;
        return _length-1;
    }
    @Override
    public void remove(int input) {
        for (DcColumn col : getColumns())
        {
            col.getData().remove(input);
        }

        _length--;
    }

    // Expression (nested record) methods: append, insert, remove, read, write.

    @Override
    public int find(ExprNode expr) {
        int[] result = java.util.stream.IntStream.range(0, getLength()).toArray(); // All elements of this set (can be quite long)

        boolean hasBeenRestricted = false; // For the case where the Length==1, and no key columns are really provided, so we get at the end result.Length==1 which is misleading. Also, this fixes the problem of having no key dimensions.

        List<DcColumn> dims = new ArrayList<DcColumn>();
        dims.addAll(getColumns().stream().filter(x -> x.isKey()).collect(Collectors.toList()));
        dims.addAll(getColumns().stream().filter(x -> !x.isKey()).collect(Collectors.toList()));

        for (DcColumn dim : dims) // OPTIMIZE: the order of dimensions matters (use statistics, first dimensins with better filtering). Also, first identity dimensions.
        {
            ExprNode childExpr = expr.getChild(dim.getName());
            if (childExpr != null)
            {
                Object val = null;
                val = childExpr.getResult().getValue();

                hasBeenRestricted = true;
                int[] range = dim.getData().deproject(val); // Deproject the value
                result = Utils.intersect(result, range); // Intersect with previous de-projections
                // OPTIMIZE: Write our own implementation for intersection and other operations. Assume that they are ordered.
                // OPTIMIZE: Remember the position for the case this value will have to be inserted so we do not have again search for this positin during insertion (optimization)

                if (result.length == 0) break; // Not found
            }
        }

        if (result.length == 0) // Not found
        {
            return -1;
        }
        else if (result.length == 1) // Found single element - return its offset
        {
            if (hasBeenRestricted) return result[0];
            else return -result.length;
        }
        else // Many elements satisfy these properties (non-unique identities). Use other methods for getting these records (like de-projection)
        {
            return -result.length;
        }
    }
    @Override
    public boolean canAppend(ExprNode expr) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int append(ExprNode expr) {

        for (DcColumn dim : getColumns()) // We must append one value to ALL greater dimensions (possibly null)
        {
            ExprNode childExpr = expr.getChild(dim.getName()); // TODO: replace by accessor by dimension reference (has to be resolved in the tuple)
            Object val = null;
            if (childExpr != null) // A tuple contains a subset of all dimensions
            {
                val = childExpr.getResult().getValue();
            }
            dim.getData().append(val);
        }

        _length++;
        return getLength() - 1;
    }

    //
    // ComTableDefinition
    //

    protected DcTableDefinitionType _definitionType;
    @Override
    public DcTableDefinitionType getDefinitionType() { return _definitionType; }
    @Override
    public void setDefinitionType(DcTableDefinitionType value) { _definitionType = value; }

    protected ExprNode _whereExpr;
    @Override
    public ExprNode getWhereExpr() { return _whereExpr; }
    public void setWhereExpr(ExprNode value) { _whereExpr = value; }

    protected ExprNode _orderbyExp;
    @Override
    public ExprNode getOrderbyExp() { return _orderbyExp; }
    public void setOrderbyExp(ExprNode value) { _orderbyExp = value; }

    @Override
    public DcIterator getWhereEvaluator() {
        DcIterator evaluator = new IteratorExpr(this);
        return evaluator;
    }

    @Override
    public void populate() {
        if (getDefinitionType() == DcTableDefinitionType.FREE)
        {
            return; // Nothing to do
        }

        setLength(0);

        if (getDefinitionType() == DcTableDefinitionType.PRODUCT) // Product of local sets (no project/de-project from another set)
        {
            //
            // Evaluator for where expression which will be used to check each new record before it is added
            //
            DcIterator eval = null;
            if (getDefinition().getWhereExpr() != null)
            {
                eval = getWhereEvaluator();
            }

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
                    int input = append(dims, vals);

                    // Now check if this appended element satisfies the where expression and if not then remove it
                    if (eval != null)
                    {
                        boolean satisfies = true;

                        eval.last();
                        eval.evaluate();
                        satisfies = (boolean)eval.getResult();

                        if (!satisfies)
                        {
                            setLength(getLength() - 1);
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

        }
        else if (getDefinitionType() == DcTableDefinitionType.PROJECTION) // There are import dimensions so copy data from another set (projection of another set)
        {
            DcColumn projectDim = getInputColumns().stream().filter(d -> d.getDefinition().isAppendData()).collect(Collectors.toList()).get(0);
            DcTable sourceSet = projectDim.getInput();
            DcTable targetSet = projectDim.getOutput(); // this set

            // Delegate to column evaluation - it will add records from column expression
            projectDim.getDefinition().evaluate();
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

        setDefinitionType(DcTableDefinitionType.FREE);
    }

}
