package com.conceptoriented.com;

import java.math.BigDecimal;
import java.time.Instant;

public class Dim implements ComColumn, ComColumnDefinition {

	//
	// CsColumn interface
	//

	protected String _name;
	@Override
	public String getName() {
		return _name;
	}
	@Override
	public void setName(String name) {
		this._name = name;
	}

	protected boolean _isKey;
	@Override
	public boolean isKey() {
		return _isKey;
	}
	@Override
	public void setKey(boolean isKey) {
		_isKey = isKey;
	}

	protected boolean _isSuper;
	@Override
	public boolean isSuper() {
		return _isSuper;
	}
	public void setSuper(boolean isSuper) {
		_isSuper = isSuper;
	}

	public boolean isPrimitive() {
		return _output == null ? false : _output.IsPrimitive;
	}

	protected ComTable _input;
	@Override
	public ComTable getInput() {
		return _input;
	}
	@Override
	public void setInput(ComTable input) {
        if (_input == input) return;
        _input = input; 
	}

	protected ComTable _output;
	@Override
	public ComTable getOutput() {
		return _output;
	}
	@Override
	public void setOutput(ComTable output) {
        if (_output == output) return;
        _output = output;
        _data = CreateColumnData(_output, this);
	}

	@Override
	public void add() {
		assert _input != null && _output != null; 

        if (_isSuper) // Only one super-dim per table can exist
        {
            if (_input != null && _input.SuperColumn != null)
            {
                _input.SuperColumn.Remove(); // Replace the existing column by the new one
            }
        }

        if (_output != null) _output.InputColumns.add(this);
        if (_input != null) _input.Columns.add(this);
	}

	@Override
	public void remove() {
		assert _input != null && _output != null; 

        if (_output != null) _output.InputColumns.Remove(this);
        if (_input != null) _input.Columns.Remove(this);
	}

	protected ComColumnData _data;
	@Override
	public ComColumnData getData() {
		return _data;
	}

	protected ComColumnDefinition _definition;
	@Override
	public ComColumnDefinition getDefinition() {
		return _definition;
	}


	
	
	
	
	
	
	//
	// CsColumnDefinition interface
	//

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	@Override
	public void evaluate() {
		ComColumn column = this;

		ComColumnEvaluator recordEvaluator = getColumnEvaluator(); // Compile formula into computing object
		// TODO Turn off indexing/sorting in the storage object as we set the function values and reindex at the end

		//
		// Determine if it is an aggregated (accumulated) function or not
		//
		boolean isAggregated = false;

		//
		// Organize a loop with record evaluations
		//
        if (isAggregated) {
        	// Loop through all inputs of the fact set and *updating* (accumulating) the function output
        	ComTable loopTable = null; // TODO
    		for (int input = 0; input < loopTable.getTableData().getLength(); input++)
            {
            	recordEvaluator.evaluateUpdate(input);
            }
        }
        else {
        	 // Loop through all inputs of this set and *setting* (writing) the function output
    		ComTable loopTable = column.getInput();
            for (int input = 0; input < loopTable.getTableData().getLength(); input++)
            {
            	recordEvaluator.evaluateSet(input);
            }
        }

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public ComColumnEvaluator getColumnEvaluator() {
		ComColumn column = this;
		ComColumnData columnData = column.getData();
		
		// TODO: Depending on the format of the formula and type of function instantiate a specific object

		return null;
	}


	
	
    public static ComColumnData CreateColumnData(ComTable type, ComColumn column)
    {
        ComColumnData colData = new DimEmpty();

        if (type == null || string.IsNullOrEmpty(type.Name))
        {
        }
        else if (StringSimilarity.SameTableName(type.Name, "Void"))
        {
        }
        else if (StringSimilarity.SameTableName(type.Name, "Top"))
        {
        }
        else if (StringSimilarity.SameTableName(type.Name, "Bottom")) // Not possible by definition
        {
        }
        else if (StringSimilarity.SameTableName(type.Name, "Root"))
        {
        }
        else if (StringSimilarity.SameTableName(type.Name, "Integer"))
        {
            colData = new DimPrimitive<Integer>(column);
        }
        else if (StringSimilarity.SameTableName(type.Name, "Double"))
        {
            colData = new DimPrimitive<Double>(column);
        }
        else if (StringSimilarity.SameTableName(type.Name, "Decimal"))
        {
            colData = new DimPrimitive<Decimal>(column);
        }
        else if (StringSimilarity.SameTableName(type.Name, "String"))
        {
            colData = new DimPrimitive<String>(column);
        }
        else if (StringSimilarity.SameTableName(type.Name, "Boolean"))
        {
            colData = new DimPrimitive<Boolean>(column);
        }
        else if (StringSimilarity.SameTableName(type.Name, "DateTime"))
        {
            colData = new DimPrimitive<LocalDate>(column);
        }
        else if (StringSimilarity.SameTableName(type.Name, "Set"))
        {
        }
        else // User (non-primitive) set
        {
            colData = new DimPrimitive<Integer>(column);
        }

        return colData;
    }

	//
	// Constructors
	//
	public Dim(String name, ComTable input, ComTable output, boolean isKey, boolean isSuper) {
		assert name != null && input != null && output != null;

		this._name = name;
		this._input = input;
		this._output = output;
		this.isKey = isKey;
		this._isSuper = isSuper;

		CsDataType dataType = output.getDataType();
        if(dataType == CsDataType.Void) {
        }
        else if(dataType == CsDataType.Top) {
        }
        else if(dataType == CsDataType.Bottom) {
        }
        else if(dataType == CsDataType.Root) {
        	_data = new DimPrimitive<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Integer) {
        	_data = new DimPrimitive<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Double) {
        	_data = new DimPrimitive<Double>(this, dataType);
        }
        else if(dataType == CsDataType.Decimal) {
        	_data = new DimPrimitive<BigDecimal>(this, dataType);
        }
        else if(dataType == CsDataType.String) {
        	_data = new DimPrimitive<String>(this, dataType);
        }
        else if(dataType == CsDataType.Boolean) {
        	_data = new DimPrimitive<Boolean>(this, dataType);
        }
        else if(dataType == CsDataType.DateTime) {
        	_data = new DimPrimitive<Instant>(this, dataType);
        }
		
        _definition = this;
	}
	
}


class DimEmpty : ComColumnData
{

    protected Offset _length;
    public Offset Length
    {
        get
        {
            return _length;
        }
        set
        {
            _length = value;
        }
    }

    public bool IsNull(Offset input) { return true; }

    public object GetValue(Offset input) { return null; }

    public void SetValue(Offset input, object value) { }

    public void NullifyValues() { }

    public void Append(object value) { }

    public void Insert(Offset input, object value) { }

    public void Remove(Offset input) { }

    public object ProjectValues(Offset[] offsets) { return null; }

    public Offset[] DeprojectValue(object value) { return null; } // Or empty array 
}



class ColumnDefinition : ComColumnDefinition 
{
    protected ComColumn Dim { get; set; }

    #region ComColumnDefinition interface

    public bool IsGenerating { get; set; }

    public ColumnDefinitionType DefinitionType { get; set; }

    public AstNode FormulaAst { get; set; }

    public ExprNode Formula { get; set; }

    public Mapping Mapping { get; set; }

    public ExprNode WhereExpr { get; set; }

    //
    // Aggregation
    //

    public ComTable FactTable { get; set; }

    public List<DimPath> GroupPaths { get; set; }

    public List<DimPath> MeasurePaths { get; set; }

    public string Updater { get; set; }

    // Aassert: FactTable.GroupFormula + ThisSet.ThisFunc = FactTable.MeasureFormula
    // Aassert: if LoopSet == ThisSet then GroupCode = null, ThisFunc = MeasureCode

    //
    // Dependencies
    //

    public List<Dim> Dependencies { get; set; } // Other functions this function directly depends upon. Computed from the definition of this function.
    // Find and store all outputs of this function by evaluating (executing) its definition in a loop for all input elements of the fact set (not necessarily this set)

    public ComColumnEvaluator GetColumnEvaluator()
    {
        // Principle: population methods are unaware of Definition type (expressions etc.) - they use only evaluator (no dependency on the definition details)

        // Here we return different types of objects that implement this interface depending on the definition type (and reflecting/based on the definition)
        // Based on Mapping - can be transformed an (tuple) expression
        // Based on tuple expression - object that can evaluate tuple tree (find, append etc.), say, an extension of a passive tuple or simply implement the Evaluator interface by the expression object
        // Based on expression - as above
        // Based on aggregation - it is update function so initially we can return a standard updater like SUM (untyped), in future, return typed updaters, and in future also custom updaters based on v-expr or other code
        // Based on library - load lib, instantiate via factory, initialize (say, resolve names), return object
        // Based on source code - compile class, instantiate, initialize (say, resolve), return instance

        ComColumnEvaluator evaluator = null;

        if (DefinitionType == ColumnDefinitionType.FREE) 
        {
            ; // Nothing to do
        }
        else if (Dim.Input.Schema != Dim.Output.Schema && Dim.Input.Schema is SetTopCsv) // Import data from a remote source
        {
            evaluator = ExprEvaluator.CreateCsvEvaluator(Dim);
        }
        else if (Dim.Input.Schema != Dim.Output.Schema && Dim.Input.Schema is SetTopOledb) // Import data from a remote source
        {
            evaluator = ExprEvaluator.CreateOledbEvaluator(Dim);
        }
        else if (DefinitionType == ColumnDefinitionType.AGGREGATION)
        {
            evaluator = ExprEvaluator.CreateAggrEvaluator(Dim);
        }
        else if (DefinitionType == ColumnDefinitionType.ARITHMETIC || DefinitionType == ColumnDefinitionType.LINK)
        {
            evaluator = ExprEvaluator.CreateColumnEvaluator(Dim);
        }
        else
        {
            throw new NotImplementedException("This type of column definition is not implemented.");
        }

        return evaluator;
    }

    //
    // Compute
    //

    public void Initialize() { }

    public void Evaluate()
    {
        ComColumnEvaluator evaluator = GetColumnEvaluator();
        if (evaluator == null) return;

        while (evaluator.Next())
        {
            evaluator.Evaluate();
        }
    }

    public void Finish() { }

    //
    // Dependencies
    //

    public List<ComTable> UsesTables(bool recursive) // This element depends upon
    {
        List<ComTable> res = new List<ComTable>();

        if (DefinitionType == ColumnDefinitionType.FREE)
        {
            ;
        }
        else if (DefinitionType == ColumnDefinitionType.ANY || DefinitionType == ColumnDefinitionType.ARITHMETIC || DefinitionType == ColumnDefinitionType.LINK)
        {
            if (Formula != null) // Dependency information is stored in expression (formula)
            {
                res = Formula.Find((ComTable)null).Select(x => x.Result.TypeTable).ToList();
            }
        }
        else if (DefinitionType == ColumnDefinitionType.AGGREGATION)
        {
            res.Add(FactTable); // This column depends on the fact table

            // Grouping and measure paths are used in this column
            if (GroupPaths != null)
            {
                foreach (var path in GroupPaths)
                {
                    foreach (var seg in path.Path)
                    {
                        if (!res.Contains(seg.Output)) res.Add(seg.Output);
                    }
                }
            }
            if (MeasurePaths != null)
            {
                foreach (var path in MeasurePaths)
                {
                    foreach (var seg in path.Path)
                    {
                        if (!res.Contains(seg.Output)) res.Add(seg.Output);
                    }
                }
            }
        }

        return res;
    }
    public List<ComTable> IsUsedInTables(bool recursive) // Dependants
    {
        List<ComTable> res = new List<ComTable>();

        // TODO: Which other sets use this function for their content? Say, if it is a generating function. Or it is a group/measure function.
        // Analyze other function definitions and check if this function is used there directly. 
        // If such a function has been found, then make the same call for it, that is find other functins where it is used.

        // A functino can be used in Filter expression and Sort expression

        return res;
    }

    public List<ComColumn> UsesColumns(bool recursive) // This element depends upon
    {
        List<ComColumn> res = new List<ComColumn>();

        if (DefinitionType == ColumnDefinitionType.FREE)
        {
            ;
        }
        else if (DefinitionType == ColumnDefinitionType.ANY || DefinitionType == ColumnDefinitionType.ARITHMETIC || DefinitionType == ColumnDefinitionType.LINK)
        {
            if (Formula != null) // Dependency information is stored in expression (formula)
            {
                res = Formula.Find((ComColumn)null).Select(x => x.Column).ToList();
            }
        }
        else if (DefinitionType == ColumnDefinitionType.AGGREGATION)
        {
            // Grouping and measure paths are used in this column
            if (GroupPaths != null)
            {
                foreach (var path in GroupPaths)
                {
                    foreach (var seg in path.Path)
                    {
                        if (!res.Contains(seg)) res.Add(seg);
                    }
                }
            }
            if (MeasurePaths != null)
            {
                foreach (var path in MeasurePaths)
                {
                    foreach (var seg in path.Path)
                    {
                        if (!res.Contains(seg)) res.Add(seg);
                    }
                }
            }
        }

        return res;
    }
    public List<ComColumn> IsUsedInColumns(bool recursive) // Dependants
    {
        List<ComColumn> res = new List<ComColumn>();

        // TODO: Find which other columns use this column in the definition

        return res;
    }

    public ColumnDefinition(ComColumn dim)
    {
        Dim = dim;

        IsGenerating = false;
        DefinitionType = ColumnDefinitionType.FREE;
        
        GroupPaths = new List<DimPath>();
        MeasurePaths = new List<DimPath>();

        Dependencies = new List<Dim>();
    }

}
