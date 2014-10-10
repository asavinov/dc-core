package com.conceptoriented.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Set implements ComTable, ComTableData, ComTableDefinition {
	
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
		return getSuperTable() instanceof ComSchema; // If its super-set is Top
	}

    // Outputs

	protected List<ComColumn> greaterDims;
	@Override
	public List<ComColumn> getColumns() {
		return greaterDims;
	}
	@Override
	public ComColumn getSuperColumn() {
		Optional<ComColumn> ret = greaterDims.stream().filter(x -> x.isSuper()).findAny();
		return ret.isPresent() ? ret.get() : null;
	}
	@Override
	public ComTable getSuperTable() {
		return getSuperColumn() != null ? getSuperColumn().getOutput() : null;
	}
	@Override
	public ComSchema getSchema() {
		ComTable t = this;
		while(t.getSuperColumn() != null) t = t.getSuperColumn().getOutput();
		return (ComSchema)t;
	}

    // Inputs
	
	protected List<ComColumn> lesserDims;
	@Override
	public List<ComColumn> getInputColumns() {
		return lesserDims;
	}
	@Override
	public List<ComColumn> getSubColumns() {
		return lesserDims.stream().filter(x -> x.isSuper()).collect(Collectors.toList());
	}
	@Override
	public List<ComTable> getSubTables() {
		return getSubColumns().stream().map(x -> x.getInput()).collect(Collectors.toList());
	}
	@Override
	public List<ComTable> getAllSubTables() {
        List<ComTable> result = new ArrayList<ComTable>();
        result.addAll(getSubTables());
        int count = result.size();
        for (int i = 0; i < count; i++)
        {
            List<ComTable> subsets = result.get(i).getAllSubTables();
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
	public boolean isSubTable(ComTable parent) { // Is subset of the specified table
        for (ComTable set = this; set != null; set = set.getSuperTable())
        {
            if (set == parent) return true;
        }
        return false;
	}
	@Override
	public boolean isInput(ComTable set) { // Is lesser than the specified table
        var paths = new PathEnumerator(this, set, DimensionType.IDENTITY_ENTITY);
        return paths.Count() > 0;
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
	public ComColumn getColumn(String name) { // Greater column
		Optional<ComColumn> ret = greaterDims.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get() : null;
	}
	@Override
	public ComTable getTable(String name) { // TODO: Greater table/type - not subtable
		Optional<ComColumn> ret = lesserDims.stream().filter(x -> x.isSuper() && x.getInput().getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get().getInput() : null;
	}
	@Override
	public ComTable getSubTable(String name) { // Subtable
		if(getName().equalsIgnoreCase(name)) return this;

		for(ComColumn c : getSubColumns()) {
			ComTable t = c.getInput().getSubTable(name);
			if(t != null) return t;
		}
		return null;
	}
    
	@Override
	public ComTableData getData() {
		return this;
	}

	@Override
	public ComTableDefinition getDefinition() {
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
        for (ComColumn col : getColumns())
        {
            col.getData().setLength(value);
        }
	}

    // Value methods

	@Override
	public Object getValue(String name, int offset) {
        ComColumn col = getColumn(name);
        return col.getData().getValue(offset);
    }
	@Override
	public void setValue(String name, int offset, Object value) {
        ComColumn col = getColumn(name);
        col.getData().setValue(offset, value);
    }
    
    // Tuple methods

	@Override
	public int find(ComColumn[] dims, Object[] values) {
        int[] result = java.util.stream.IntStream.range(0, getLength()).toArray(); // All elements of this set (can be quite long)


        boolean hasBeenRestricted = false; // For the case where the Length==1, and no key columns are really provided, so we get at the end result.Length==1 which is misleading. Also, this fixes the problem of having no key dimensions.
        for (int i = 0; i < dims.length; i++)
        {
            hasBeenRestricted = true;
            int[] range = dims[i].getData().deproject(values[i]); // Deproject one value
            result = result.Intersect(range).ToArray(); 
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
	public int append(ComColumn[] dims, Object[] values) {
        for (int i = 0; i < dims.length; i++)
        {
            dims[i].getData().append(values[i]);
        }

        _length++;
        return _length-1;
	}
	@Override
	public void remove(int input) {
        for (ComColumn col : getColumns())
        {
            col.getData().remove(input);
        }

        _length--;
	}
	
    // Expression (nested record) methods: append, insert, remove, read, write.

	@Override
	public int find(ExprNode expr) {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean canAppend(ExprNode expr) {
		throw new UnsupportedOperationException();
	}
	@Override
	public int append(ExprNode expr) {
		throw new UnsupportedOperationException();
	}
	
	//
	// ComTableDefinition
	//

	protected TableDefinitionType _definitionType;
	@Override
    public TableDefinitionType getDefinitionType() { return _definitionType; }
	@Override
	public void setDefinitionType(TableDefinitionType value) { _definitionType = value; }

    protected ExprNode _whereExpr;
	@Override
	public ExprNode getWhereExpr() { return _whereExpr; }
	public void setWhereExpr(ExprNode value) { _whereExpr = value; }

    protected ExprNode _orderbyExp;
	@Override
	public ExprNode getOrderbyExp() { return _orderbyExp; }
	public void setOrderbyExp(ExprNode value) { _orderbyExp = value; }

	@Override
    public ComEvaluator getWhereEvaluator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void populate() {
		throw new UnsupportedOperationException();
	}

	@Override
    public void unpopulate() {
		throw new UnsupportedOperationException();
	}

    // Dependencies. The order is important and corresponds to dependency chain

	@Override
	public List<ComTable> usesTables(boolean recursive) { // This element depends upon
		throw new UnsupportedOperationException();
	}
	@Override
	public List<ComTable> isUsedInTables(boolean recursive) { // Dependants
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ComColumn> usesColumns(boolean recursive) { // This element depends upon
		throw new UnsupportedOperationException();
	}
	@Override
	public List<ComColumn> isUsedInColumns(boolean recursive) { // Dependants
		throw new UnsupportedOperationException();
	}
	
	//
	// Constructors
	//
	
	public Set(String name, ComDataType dataType) {
		this.name = name;
		
		greaterDims = new ArrayList<ComColumn>();
		lesserDims = new ArrayList<ComColumn>();
	}

}
