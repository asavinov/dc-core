package com.conceptoriented.com;

public class DimPrimitive<T extends Comparable<T>> implements ComColumnData {

    private T[] _cells; // Each cell contains a T value in arbitrary original order
    private int[] _offsets; // Each cell contains an offset to an element in cells in ascending or descending order

    private int _nullCount; // Nulls are stored in the beginning of array of indexes (that is, treated as absolute minimum)
    private T _nullValue; // It is what is written in cell instead of null if null is not supported by the type. If null is supported then null is stored (instead, we can use NullValue=null).

    // Memory management parameters for instances (used by extensions and in future will be removed from this class).
    protected static int initialSize = 1024 * 8; // In elements
    protected static int incrementSize = 1024; // In elements

    protected int allocatedSize; // How many elements (maximum) fit into the allocated memory

    protected boolean _autoindex = true ;// If true then index will be automatically maintained. If false then indexing has to be done manually.
    
	//
	// CsDataColumn interface
	//

	protected CsDataType _dataType;
	@Override
	public CsDataType getDataType() {
		return _dataType;
	}

	protected int _length;
    @Override
	public int getLength() {
		return _length;
	}
	@Override
	public void setLength(int newLength) {
        if (newLength == _length) return;

        // Ensure that there is enough memory
        if (newLength > allocatedSize) // Not enough storage for the new element
        {
            allocatedSize += incrementSize * ((newLength - allocatedSize) / incrementSize + 1);
            _cells = java.util.Arrays.copyOf(_cells, allocatedSize); // Resize the storage for values
            _offsets = java.util.Arrays.copyOf(_offsets, allocatedSize); // Resize the indeex
        }

        // Update data and index in the case of increase (append to last) and decrease (delete last)
        if (newLength > _length)
        {
            while (newLength > _length) appendValue(null); // OPTIMIZE: Instead of appending individual values, write a method for appending an interval of offset (with default value)
        }
        else if (newLength < _length)
        {
            // TODO: remove last elements
        }
	}

	@Override
	public boolean isNullValue(int input) {
        // For non-nullable storage, use the index to find if this cell is in the null interval of the index (beginning)
        int pos = FindIndex(input);
        return pos < _nullCount;
        // For nullable storage: simply check the value (actually this method is not needed for nullable storage because the user can compare the values returned from GetValue)
        // return EqualityComparer<T>.Default.Equals(_nullValue, _cells[offset]);
	}

	@Override
	public Object getValue(int input) {
		return _cells[input];
	}

	@Override
	public void setValue(int input, Object value) {
        T val = null;
        int oldPos = FindIndex(input); // Old sorted position of the cell we are going to change
        int[] interval;
        int pos = -1; // New sorted position for this cell

        if (value == null)
        {
            val = _nullValue;
            interval = new int[] {0, _nullCount};

            if (oldPos >= _nullCount) _nullCount++; // If old value is not null, then increase the number of nulls
        }
        else
        {
            val = (T)value;
            interval = FindIndexes(val);

            if (oldPos < _nullCount) _nullCount--; // If old value is null, then decrease the number of nulls
        }

        // Find sorted position within this value interval (by increasing offsets)
        pos = java.util.Arrays.binarySearch(_offsets, interval[0], interval[1], input);
        if (pos < 0) pos = ~pos;

        if (pos > oldPos)
        {
        	System.arraycopy(_offsets, oldPos + 1, _offsets, oldPos, (pos - 1) - oldPos); // Shift backward by overwriting old
            _offsets[pos - 1] = input;
        }
        else if (pos < oldPos)
        {
        	System.arraycopy(_offsets, pos, _offsets, pos + 1, oldPos - pos); // Shift forward by overwriting old pos
            _offsets[pos] = input;
        }

        _cells[input] = val;
	}

	@Override
    public void nullifyValues() // Reset values and index to initial state (all nulls)
    {
    	// TODO:
    }

	@Override
    public void appendValue(Object value)
    {
        // Ensure that there is enough memory
        if (_length == allocatedSize) // Not enough storage for the new element (we need Length+1)
        {
            allocatedSize += incrementSize;

            _cells = java.util.Arrays.copyOf(_cells, allocatedSize); // Resize the storage for values
            _offsets = java.util.Arrays.copyOf(_offsets, allocatedSize); // Resize the indeex
        }

        T val = null;
        int[] interval;
        int pos = -1;

        if (value == null)
        {
            val = _nullValue;
            interval = new int[] {0, _nullCount};
            _nullCount++;
        }
        else
        {
            val = (T)value;
            interval = FindIndexes(val);
        }

        pos = interval[1]; // New value has the largest offset and hence is inserted after the end of the interval of values

    	System.arraycopy(_offsets, pos, _offsets, pos + 1, _length - pos); // Free an index element by shifting other elements forward

        _cells[_length] = val;
        _offsets[pos] = _length;
        _length = _length + 1;
    }

	@Override
    public void insertValue(int input, Object value)
    {
        // TODO: 
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

	public DimPrimitive() {
	}

    public DimPrimitive(ComColumn column, CsDataType dataType) {
    	
    	_dataType = dataType;
    	
        _length = 0;
        allocatedSize = initialSize;
        _cells = (T[]) java.lang.reflect.Array.newInstance(_cells.getClass(), allocatedSize);
        _offsets = new int[allocatedSize];
	
        _nullCount = _length;
        
        // Initialize what representative value will be used instead of nulls
        _nullValue = null;
        if(_dataType == CsDataType.Integer) {
        	_nullValue = (T) (Object) Integer.MIN_VALUE;
        }
        else if(_dataType == CsDataType.Double) {
        	_nullValue = (T) (Object) Double.NaN;
        }
        else if(_dataType == CsDataType.Decimal) {
        	_nullValue = (T) (Object) Double.NaN;
        }
        else if(_dataType == CsDataType.String) {
        	_nullValue = (T) (Object) null;
        }
        else if(_dataType == CsDataType.Boolean) {
        	_nullValue = (T) (Object) null;
        }
        else if(_dataType == CsDataType.DateTime) {
        	_nullValue = (T) (Object) null;
        }

    }

}
