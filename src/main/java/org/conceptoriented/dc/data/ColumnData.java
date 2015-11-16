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

package org.conceptoriented.dc.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.conceptoriented.dc.utils.*;
import org.conceptoriented.dc.schema.*;
import org.conceptoriented.dc.data.query.ExprBuilder;

public class ColumnData<T extends Comparable<T>> implements DcColumnData {

    protected DcColumn _column;

    // Memory management parameters for instances (used by extensions and in future will be removed from this class).
    protected static int initialSize = 1024 * 10; // In elements
    protected static int incrementSize = 1024 * 2; // In elements

    //
    // Storage for the values of the column
    //
    protected int allocatedSize; // How many elements (maximum) fit into the allocated memory
    private T[] _cells; // Each cell contains a T value in arbitrary original order

    private boolean[] _nullCells; // True if the cell is null and false if it is not null. The field is used only if nulls are not values.
    private boolean _nullAsValue; // Null is a normal value with a special meaning
    private T _nullValue; // If null is a normal value this it is what is this null value. If null is not a value then this field is ignored.
    private int _nullCount; // Nulls are stored in the beginning of array of indexes (that is, treated as absolute minimum)

    //
    // Index
    //
    private int[] _offsets; // Each cell contains an offset to an element in cells in ascending or descending order

    //
    // ComColumnData interface
    //

    protected int _length;
    @Override
    public int getLength() {
        return _length;
    }
    @Override
    public void setLength(int value) {
        if (value == _length) return;

        // Ensure that there is enough memory
        if (value > allocatedSize) // Not enough storage for the new element
        {
            allocatedSize += incrementSize * ((value - allocatedSize) / incrementSize + 1);
            _cells = java.util.Arrays.copyOf(_cells, allocatedSize); // Resize the storage for values
            _offsets = java.util.Arrays.copyOf(_offsets, allocatedSize); // Resize the index
        }

        // Update data and index in the case of increase (append to last) and decrease (delete last)
        if (value > _length)
        {
            while (value > _length) append(null);
            // OPTIMIZE: Instead of appending individual values, write a method for appending an interval of offset (with default value)
        }
        else if (value < _length)
        {
            while (value < _length) remove(_length - 1);
            // OPTIMIZE: remove last elements directly
        }
    }

    protected boolean _autoIndex;
    @Override
    public boolean isAutoIndex() {
        return _autoIndex;
    }
    @Override
    public void setAutoIndex(boolean value) {
        _autoIndex = value;
    }

    protected boolean _indexed;
    @Override
    public boolean isIndexed() {
        return _indexed;
    }

    @Override
    public void reindex() {
        // Index sort in Java: http://stackoverflow.com/questions/951848/java-array-sort-quick-way-to-get-a-sorted-list-of-indices-of-an-array
        Integer[] tempIndex = new Integer[_length];

        // Reset offsets before sorting (so it will be completely new sort)
        for (int i = 0; i < _length; i++)
        {
            tempIndex[i] = i; // Now each offset represents (references) an element of the function (from domain) but they are unsorted
        }

        if(_nullAsValue) {
            java.util.Arrays.sort(tempIndex, 0, _length, new java.util.Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    if (_cells[o1] == _nullValue) {
                        return (_cells[o2] == _nullValue) ? 0 : -1;
                    }
                    if (_cells[o2] == _nullValue) {
                        return 1;
                    }
                    return _cells[o1].compareTo(_cells[o2]);
                }
                });
        }
        else {
            java.util.Arrays.sort(tempIndex, _nullCount, _length, new java.util.Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return _cells[o1].compareTo(_cells[o2]);
                }
                });
        }

        for (int i = 0; i < _length; i++)
        {
            _offsets[i] = tempIndex[i];
        }

        _indexed = true;
    }

    @Override
    public boolean isNull(int input) {
        if(_nullAsValue) {
            // For nullable storage: simply check the value (actually this method is not needed for nullable storage because the user can compare the values returned from GetValue)
            return _cells[input] == _nullValue;
        }
        else {
            // For non-nullable storage, use the index to find if this cell is in the null interval of the index (beginning)
            int pos = FindIndex(input);
            return pos < _nullCount;
        }
    }

    @Override
    public Object getValue(int input) {
        return _cells[input];
    }

    @Override
    public void setValue(int input, Object value) {

        T val = null;
        if (value == null) {
            val = _nullValue;
        }
        else {
            val = (T)toThisType(value);
        }

        // No indexing required
        if(!isAutoIndex()) {
            _offsets[_length] = _length; // It will be last in sorted order
            _cells[input] = val;
            _indexed = false; // Mark index as dirty
            return;
        }

        //
        // Index
        //

        // 1. Old sorted position of the cell we are going to overwrite
        int oldPos = FindIndex(input);

        // 2. Find the new position in index of the new value to be written at this offset

        // 2.1 Find an interval for the new value (FindIndexes)
        int[] interval;
        if (value == null)
        {
            interval = new int[] {0, _nullCount};
            if (oldPos >= _nullCount) _nullCount++; // If old value is not null, then increase the number of nulls
        }
        else
        {
            interval = FindIndexes(val);
            if (oldPos < _nullCount) _nullCount--; // If old value is null, then decrease the number of nulls
        }

        // 2.2 Find sorted position within this value interval (by increasing offsets)
        int pos = java.util.Arrays.binarySearch(_offsets, interval[0], interval[1], input);
        if (pos < 0) pos = ~pos;

        // 3. Finally simply change the position by shifting the index elements accordingly
        if (pos > oldPos)
        {
            System.arraycopy(_offsets, oldPos + 1, _offsets, oldPos, (pos - 1) - oldPos); // Shift backward by overwriting old
            pos = pos - 1;
        }
        else if (pos < oldPos)
        {
            System.arraycopy(_offsets, pos, _offsets, pos + 1, oldPos - pos); // Shift forward by overwriting old pos
        }

        _offsets[pos] = input;
        _cells[input] = val;
    }

    @Override
    public void setValue(Object value) {
        if (value == null)
        {
            nullify();
            return;
        }

        T val = (T)toThisType(value);
        for (int i = 0; i < _length; i++)
        {
            _offsets[i] = i;
            _cells[i] = val;
        }

        _nullCount = 0;
        _indexed = true;
    }

    @Override
    public void nullify() // Reset values and index to initial state (all nulls)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void append(Object value)
    {
        // Ensure that there is enough memory
        if (_length == allocatedSize) // Not enough storage for the new element (we need Length+1)
        {
            allocatedSize += incrementSize;

            _cells = java.util.Arrays.copyOf(_cells, allocatedSize); // Resize the storage for values
            _offsets = java.util.Arrays.copyOf(_offsets, allocatedSize); // Resize the index
        }

        T val = null;
        if (value == null) {
            val = _nullValue;
        }
        else {
            val = (T)toThisType(value);
        }

        // No indexing required
        if(!isAutoIndex()) {
            _offsets[_length] = _length; // It will be last in sort
            _cells[_length] = val;
            _length = _length + 1;

            _indexed = false;
            return;
        }

        //
        // Index
        //
        int[] interval;
        if (value == null) {
            interval = new int[] {0, _nullCount};
            _nullCount++;
        }
        else {
            interval = FindIndexes(val);
        }

        int pos = interval[1]; // New value has the largest offset and hence is inserted after the end of the interval of values
        System.arraycopy(_offsets, pos, _offsets, pos + 1, _length - pos); // Free an index element by shifting other elements forward

        _offsets[pos] = _length; // Update index
        _cells[_length] = val; // Update storage
        _length = _length + 1;
    }

    @Override
    public void insert(int input, Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(int input)
    {
        int pos = FindIndex(input);

        System.arraycopy(_offsets, pos + 1, _offsets, pos, _length - pos - 1); // Remove this index element by shifting all next elements backward

        // If it was null value then decrease also their count
        if (pos < _nullCount)
        {
            _nullCount--;
        }

        _length = _length - 1;
    }

    @Override
    public Object project(int[] offsets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] deproject(Object value) {
        if (value == null || !value.getClass().isArray())
        {
            return deprojectValue((T)toThisType(value));
        }
        else
        {
            return deproject((T[])value);
        }
    }

    //
    // The former DcColumnDefinition interface
    //

    protected String _formula;
    @Override
    public String getFormula() { return _formula; }
    @Override
    public void setFormula(String value)
    {
        _formula = value;
		
        if (Utils.isNullOrEmpty(value)) return;

        ExprBuilder exprBuilder = new ExprBuilder();
        ExprNode expr = exprBuilder.build(_formula);

        setFormulaExpr(expr);
    }

    protected ExprNode _formulaExpr;
    @Override
    public ExprNode getFormulaExpr() { return _formulaExpr; }
    @Override
    public void setFormulaExpr(ExprNode value) { _formulaExpr = value; }

    //
    // Structured (object) representation
    //

    protected boolean _appendData;
    public boolean isAppendData() { return _appendData; }
    public void setAppendData(boolean value) { _appendData = value; }

    protected boolean _appendSchema;
    public boolean isAppendSchema() { return _appendSchema; }
    public void setAppendSchema(boolean value) { _appendSchema = value; }

    //
    // Compute
    //

    @Override
    public void evaluate()
    {
        if (getFormulaExpr() == null || getFormulaExpr().getDefinitionType() == ColumnDefinitionType.FREE)
        {
            return; // Nothing to evaluate
        }
    	
        // General parameters
        DcSpace workspace = _column.getInput().getSchema().getSpace();
        DcColumnData columnData = _column.getData();

        _column.getData().setAutoIndex(false);
        //_column.getData().nullify();
    
        Object thisCurrent = null;
        if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.ARITHMETIC || getFormulaExpr().getDefinitionType() == ColumnDefinitionType.LINK)
        {
            if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.LINK)
            {
            	// Adjust the expression according to other parameters of the definition
                if(isAppendData()) 
                {
                	getFormulaExpr().setAction(ActionType.APPEND);
                }
                else
                {
                	getFormulaExpr().setAction(ActionType.READ);
                }
            }

            // Prepare parameter variables for the expression 
            DcTable thisTable = _column.getInput();
            DcVariable thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
            thisVariable.setTypeSchema(thisTable.getSchema());
            thisVariable.setTypeTable(thisTable);
            
            // Parameterize expression and resolve it (bind names to real objects) 
            getFormulaExpr().getOutputVariable().setSchemaName(_column.getOutput().getSchema().getName());
            getFormulaExpr().getOutputVariable().setTypeName(_column.getOutput().getName());
            getFormulaExpr().getOutputVariable().setTypeSchema(_column.getOutput().getSchema());
            getFormulaExpr().getOutputVariable().setTypeTable(_column.getOutput());
            getFormulaExpr().evaluateAndResolveSchema(workspace, new ArrayList<DcVariable>(Arrays.asList(thisVariable)));
            
        	getFormulaExpr().evaluateBegin();
            DcTableReader tableReader = thisTable.getData().getTableReader();
        	tableReader.open();
            while ((thisCurrent = tableReader.next()) != null)
            {
                thisVariable.setValue(thisCurrent); // Set parameters of the expression

                getFormulaExpr().evaluate(); // Evaluate the expression

                if (columnData != null)
                {
                    Object newValue = getFormulaExpr().getOutputVariable().getValue();
                    columnData.setValue((int)thisCurrent, newValue);
                }
            }
        	tableReader.close();
            getFormulaExpr().evaluateEnd();
        }
        else if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.AGGREGATION)
        {
            // Facts
            ExprNode factsNode = getFormulaExpr().getChild("facts").getChild(0);

            // This table and variable
            String thisTableName = factsNode.getName();
            DcTable thisTable = _column.getInput().getSchema().getSubTable(thisTableName);
            DcVariable thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
            thisVariable.setTypeSchema(thisTable.getSchema());
            thisVariable.setTypeTable(thisTable);

            // Groups
            ExprNode groupExpr; // Returns a group this fact belongs to, is stored in the group variable
            ExprNode groupsNode = getFormulaExpr().getChild("groups").getChild(0);
            groupExpr = groupsNode;
            groupExpr.evaluateAndResolveSchema(workspace, Arrays.asList(thisVariable));

            DcVariable groupVariable; // Stores current group (input for the aggregated function)
            groupVariable = new Variable(_column.getInput().getSchema().getName(), _column.getInput().getName(), "this");
            groupVariable.setTypeSchema(_column.getInput().getSchema());
            groupVariable.setTypeTable(_column.getInput());

            // Measure
            ExprNode measureExpr; // Returns a new value to be aggregated with the old value, is stored in the measure variable
            ExprNode measureNode = getFormulaExpr().getChild("measure").getChild(0);
            measureExpr = measureNode;
            measureExpr.evaluateAndResolveSchema(workspace, Arrays.asList(thisVariable));

            DcVariable measureVariable; // Stores new value (output for the aggregated function)
            measureVariable = new Variable(_column.getOutput().getSchema().getName(), _column.getOutput().getName(), "value");
            measureVariable.setTypeSchema(_column.getOutput().getSchema());
            measureVariable.setTypeTable(_column.getOutput());

            // Updater/aggregation function
            ExprNode updaterExpr = getFormulaExpr().getChild("aggregator").getChild(0);

            ExprNode outputExpr;
            outputExpr = ExprNode.createUpdater(_column, updaterExpr.getName());
            outputExpr.evaluateAndResolveSchema(workspace, Arrays.asList(groupVariable, measureVariable));
        
            getFormulaExpr().evaluateBegin();
            DcTableReader tableReader = thisTable.getData().getTableReader();
            tableReader.open();
            while ((thisCurrent = tableReader.next()) != null)
            {
            	thisVariable.setValue(thisCurrent); // Set parameters of the expression

                groupExpr.evaluate();
                int groupElement = (int)groupExpr.getOutputVariable().getValue();
                groupVariable.setValue(groupElement);

                measureExpr.evaluate();
                Object measureValue = measureExpr.getOutputVariable().getValue();
                measureVariable.setValue(measureValue);

                outputExpr.evaluate(); // Evaluate the expression

                // Write the result value to the function
                if (columnData != null)
                {
                    Object newValue = outputExpr.getOutputVariable().getValue();
                    columnData.setValue(groupElement, newValue);
                }
            }
            tableReader.close();
            getFormulaExpr().evaluateEnd();
        }
        else
        {
            throw new UnsupportedOperationException("This type of column definition is not implemented.");
        }

        _column.getData().reindex();
        _column.getData().setAutoIndex(true);
    }

    //
    // Dependencies
    //

    public List<Column> dependencies;

    @Override
    public List<DcTable> usesTables(boolean recursive) // This element depends upon
    {
        throw new UnsupportedOperationException("TODO");
    }
    @Override
    public List<DcTable> isUsedInTables(boolean recursive) // Dependants
    {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<DcColumn> usesColumns(boolean recursive) // This element depends upon
    {
        throw new UnsupportedOperationException("TODO");
    }
    @Override
    public List<DcColumn> isUsedInColumns(boolean recursive) // Dependants
    {
        throw new UnsupportedOperationException("TODO");
    }

    
    //
    // Index methods
    //

    private int FindIndex(int offset) // Find an index for an offset of a cell (rather than a value in this cell)
    {
        // A value can be stored at many different offsets while one offset has always one index and therefore a single valueis returned rather than an interval.

        // First, we try to find it in the null interval

        int pos = java.util.Arrays.binarySearch(_offsets, 0, _nullCount, offset);
        if (pos >= 0 && pos < _nullCount) return pos; // It is null

        // Second, try to find it as a value (find the value area and then find the offset in the value interval)
        int[] indexes = FindIndexes(_cells[offset]);
        pos = java.util.Arrays.binarySearch(_offsets, indexes[0], indexes[1], offset);
        if (pos >= indexes[0] && pos < indexes[1]) return pos;

        return -1; // Not found (error - all valid offset must be present in the index)
    }

    private int[] FindIndexes(T value)
    {
        // Returns an interval of indexes which all reference the specified value
        // min is inclusive and max is exclusive
        // min<max - the value is found between [min,max)
        // min=max - the value is not found, min=max is the position where it has to be inserted
        // min=length - the value has to be appended (and is not found, so min=max)

        // Alternative: Array.BinarySearch<T>(mynumbers, value) or  BinarySearch<T>(T[], Int32, Int32, T) - search in range
        // Comparer<T> comparer = Comparer<T>.Default;
        // mid = Array.BinarySearch(_offsets, 0, _count, value, (a, b) => comparer.Compare(_cells[a], _cells[b]));
        //IComparer<T> comparer = new IndexComparer<T>(this);
        //mid = Array.BinarySearch(_offsets, 0, _count, value, comparer);

        // Binary search in a sorted array with ascending values: http://stackoverflow.com/questions/8067643/binary-search-of-a-sorted-array
        int mid = _nullCount, first = _nullCount, last = _length;
        while (first < last)
        {
            mid = (first + last) / 2;

            int comp = value.compareTo(_cells[_offsets[mid]]);
            if (comp > 0) // Less: target > mid
            {
                first = mid+1;
            }
            else if (comp < 0) // Greater: target < mynumbers[mid]
            {
                last = mid;
            }
            else
            {
                break;
            }
        }

        if (first == last) // Not found
        {
            return new int[] { first, last };
        }

        // One element is found. Now find min and max positions for the interval of equal values.
        // Optimization: such search is not efficient - it is simple scan. One option would be use binary serach within interval [first, mid] and [mid, last]
        for (first = mid; first >= _nullCount && value.equals(_cells[_offsets[first]]); first--)
            ;
        for (last = mid; last < _length && value.equals(_cells[_offsets[last]]); last++)
            ;

        return new int[] { first+1, last };
    }

    protected int[] deprojectValue(T value)
    {
        int[] indexes = new int[2];

        if (value == null)
        {
            indexes[0] = 0;
            indexes[1] = _nullCount;
        }
        else
        {
            indexes = FindIndexes(value);
        }

        if (indexes[0] == indexes[1])
        {
            return new int[0]; // Not found
        }

        int[] result = new int[indexes[1] - indexes[0]];

        for (int i = 0; i < result.length; i++)
        {
            // OPTIMIZE: Use system copy function
            result[i] = _offsets[indexes[0] + i];
        }

        return result;
    }

    protected int[] deproject(T[] values)
    {
        throw new UnsupportedOperationException("TODO");
    }

    protected Object toThisType(Object value)
    {
        if(_column == null || _column.getOutput() == null) return value;

        String type = _column.getOutput().getName();
        switch(type) {
        case "Integer" : return Utils.toInt32(value);
        case "Double" : return Utils.toDouble(value);
        case "Decimal" : return Utils.toDecimal(value);
        case "String" : return value.toString();
        case "Boolean" : return Utils.toBoolean(value);
        case "DateTime" : return Utils.toDateTime(value);
        default: return value;
        }
    }

    public ColumnData(DcColumn col) {
        // TODO: Check if output (greater) set is of correct type

        _column = col;

        allocatedSize = initialSize;
        _cells = (T[]) new Comparable[allocatedSize];
        // _cells = (T[]) java.lang.reflect.Array.newInstance(_cells.getClass(), allocatedSize); // DOES NOT WORK because we do not know generic type at run-time:

        _nullCells = null;
        _nullAsValue = true;
        _nullValue = null; // JavaNote: Java does not have primitive generics (we have only object references) and also does not store generic info at run-time
        _nullCount = _length;

        _offsets = new int[allocatedSize];
        _autoIndex = true;
        _indexed = true;

        _length = 0;
        setLength(col.getInput().getData().getLength());

        dependencies = new ArrayList<Column>();
    }
}

