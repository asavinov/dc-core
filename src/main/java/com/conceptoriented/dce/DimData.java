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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

public class DimData<T extends Comparable<T>> implements DcColumnData {

    protected DcColumn _dim;

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
        if(_dim == null || _dim.getOutput() == null) return value;

        String type = _dim.getOutput().getName();
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

    public DimData(DcColumn dim) {
        // TODO: Check if output (greater) set is of correct type

        _dim = dim;

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
        setLength(dim.getInput().getData().getLength());
    }
}

class DimEmpty implements DcColumnData
{

    protected int _length;
    @Override
    public int getLength() {
        return _length;
    }
    @Override
    public void setLength(int value) {
        _length = value;
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
    public void reindex() { }

    @Override
    public boolean isNull(int input) { return true; }

    @Override
    public Object getValue(int input) { return null; }

    @Override
    public void setValue(int input, Object value) { }
    @Override
    public void setValue(Object value) { }

    @Override
    public void nullify() { }

    @Override
    public void append(Object value) { }

    @Override
    public void insert(int input, Object value) { }

    @Override
    public void remove(int input) { }

    @Override
    public Object project(int[] offsets) { return null; }

    @Override
    public int[] deproject(Object value) { return null; } // Or empty array
}

class ColumnDefinition implements DcColumnDefinition
{
    protected DcColumn _dim;

    //
    // ComColumnDefinition interface
    //

    protected boolean _appendData;
    public boolean isAppendData() { return _appendData; }
    public void setAppendData(boolean value) { _appendData = value; }

    protected boolean _appendSchema;
    public boolean isAppendSchema() { return _appendSchema; }
    public void setAppendSchema(boolean value) { _appendSchema = value; }

    protected DcColumnDefinitionType _definitionType;
    @Override
    public DcColumnDefinitionType getDefinitionType() { return _definitionType; }
    @Override
    public void setDefinitionType(DcColumnDefinitionType value) { _definitionType = value; }

    //
    // COEL (language) representation
    //

    protected String _formula;
    @Override
    public String getFormula() { return _formula; }
    @Override
    public void setFormula(String value)
    {
        _formula = value;

        ExprBuilder exprBuilder = new ExprBuilder();
        ExprNode expr = exprBuilder.build(_formula);
        setFormulaExpr(expr);

        if(expr.getOperation() == OperationType.TUPLE) {
            setDefinitionType(DcColumnDefinitionType.LINK);
        }
        else if(expr.getOperation() == OperationType.CALL && expr.getName().equalsIgnoreCase("AGGREGATE")) {
            setDefinitionType(DcColumnDefinitionType.AGGREGATION);
        }
        else {
            setDefinitionType(DcColumnDefinitionType.ARITHMETIC);
        }
    }

    //
    // Structured (object) representation
    //

    protected ExprNode _formulaExpr;
    @Override
    public ExprNode getFormulaExpr() { return _formulaExpr; }
    @Override
    public void setFormulaExpr(ExprNode value) { _formulaExpr = value; }

    protected Mapping _mapping;
    @Override
    public Mapping getMapping() { return _mapping; }
    @Override
    public void setMapping(Mapping value) { _mapping = value; }

    protected ExprNode _whereExpr;
    @Override
    public ExprNode getWhereExpr() { return _whereExpr; }
    @Override
    public void setWhereExpr(ExprNode value) { _whereExpr = value; }

    //
    // Aggregation
    //

    protected DcTable _factTable;
    @Override
    public DcTable getFactTable() { return _factTable; }
    @Override
    public void setFactTable(DcTable value) { _factTable = value; }

    protected List<DimPath> _groupPaths;
    @Override
    public List<DimPath> getGroupPaths() { return _groupPaths; }
    @Override
    public void setGroupPaths(List<DimPath> value) { _groupPaths = value; }

    protected List<DimPath> _measurePaths;
    @Override
    public List<DimPath> getMeasurePaths() { return _measurePaths; }
    @Override
    public void setMeasurePaths(List<DimPath> value) { _measurePaths = value; }

    protected String _updater;
    @Override
    public String getUpdater() { return _updater; }
    @Override
    public void setUpdater(String value) { _updater = value; }

    //
    // Compute
    //

    // Get an object which is used to compute the function values according to the formula
    protected DcIterator getIterator()
    {
        DcIterator evaluator = null;

        if (getDefinitionType() == DcColumnDefinitionType.FREE)
        {
            ; // Nothing to do
        }
        else if (getDefinitionType() == DcColumnDefinitionType.AGGREGATION)
        {
            evaluator = new IteratorAggr(_dim);
        }
        else if (getDefinitionType() == DcColumnDefinitionType.ARITHMETIC)
        {
            evaluator = new IteratorExpr(_dim);
        }
        else if (getDefinitionType() == DcColumnDefinitionType.LINK)
        {
            evaluator = new IteratorExpr(_dim);
        }
        else
        {
            throw new UnsupportedOperationException("This type of column definition is not implemented.");
        }

        return evaluator;
    }

    private void evaluateBegin()
    {
        _dim.getData().setAutoIndex(false);
        //_dim.getData().nullify();
    }

    @Override
    public void evaluate()
    {
        DcIterator evaluator = getIterator();
        if (evaluator == null) return;

        try {
            evaluateBegin();

            while (evaluator.next())
            {
                evaluator.evaluate();
            }
        } catch (Exception e) {
            throw e;
        }
        finally {
            evaluateEnd();
        }

    }

    protected void evaluateEnd()
    {
        _dim.getData().reindex();
        _dim.getData().setAutoIndex(true);
    }

    //
    // Dependencies
    //

    public List<Dim> dependencies;

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

    public ColumnDefinition(DcColumn dim)
    {
        _dim = dim;

        _appendData = false;
        _definitionType = DcColumnDefinitionType.FREE;

        _groupPaths = new ArrayList<DimPath>();
        _measurePaths = new ArrayList<DimPath>();

        dependencies = new ArrayList<Dim>();
    }

}
