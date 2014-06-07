package com.conceptoriented.com;

public class ComDataColumn<T extends Comparable<T>> implements CsDataColumn {

    private T[] _cells; // Each cell contains a T value in arbitrary original order
    private int[] _offsets; // Each cell contains an offset to an element in cells in ascending or descending order

    private int _nullCount; // Nulls are stored in the beginning of array of indexes (that is, treated as absolute minimum)
    private T _nullValue; // It is what is written in cell instead of null if null is not supported by the type. If null is supported then null is stored (instead, we can use NullValue=null).

    // Memory management parameters for instances (used by extensions and in future will be removed from this class).
    protected static int initialSize = 1024 * 8; // In elements
    protected static int incrementSize = 1024; // In elements

    protected int allocatedSize; // How many elements (maximum) fit into the allocated memory




    @Override
	public CsDataType getDataType() {
		// TODO Auto-generated method stub
		return null;
	}

    protected int _length;
    @Override
	public int getLength() {
		return _length;
	}
	@Override
	public void setLength(int newLength) {
        if (newLength == this._length) return;

        // Ensure that there is enough memory
        if (newLength > allocatedSize) // Not enough storage for the new element
        {
            allocatedSize += incrementSize * ((newLength - allocatedSize) / incrementSize + 1);
            _cells = java.util.Arrays.copyOf(_cells, allocatedSize); // Resize the storage for values
            _offsets = java.util.Arrays.copyOf(_offsets, allocatedSize); // Resize the indeex
        }

        // Update data and index in the case of increase (append to last) and decrease (delete last)
        if (newLength > this._length)
        {
            while (newLength > this._length) Append(null); // OPTIMIZE: Instead of appending individual values, write a method for appending an interval of offset (with default value)
        }
        else if (newLength < this._length)
        {
            // TODO: remove last elements
        }
	}

	@Override
	public boolean IsNullValue(int input) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValue(int input) {
		return _cells[input];
	}

	@Override
	public void setValue(int input, Object value) {
		// TODO Auto-generated method stub

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

}
