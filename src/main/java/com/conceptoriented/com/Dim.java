package com.conceptoriented.com;

import java.math.BigDecimal;
import java.time.Instant;

public class Dim implements ComColumn {

	//
	// ComColumn interface
	//

	protected String _name;
	@Override
	public String getName() {
		return _name;
	}
	@Override
	public void setName(String value) {
		this._name = value;
	}

	protected boolean _key;
	@Override
	public boolean isKey() {
		return _key;
	}
	@Override
	public void setKey(boolean value) {
		_key = value;
	}

	protected boolean _super;
	@Override
	public boolean isSuper() {
		return _super;
	}
	public void setSuper(boolean value) {
		_super = value;
	}

	@Override
	public boolean isPrimitive() {
		return _output == null ? false : _output.isPrimitive();
	}

	protected ComTable _input;
	@Override
	public ComTable getInput() {
		return _input;
	}
	@Override
	public void setInput(ComTable value) {
        if (_input == value) return;
        _input = value; 
	}

	protected ComTable _output;
	@Override
	public ComTable getOutput() {
		return _output;
	}
	@Override
	public void setOutput(ComTable value) {
        if (_output == value) return;
        _output = value;
        _data = CreateColumnData(_output, this);
	}

	@Override
	public void add() {
		assert _input != null && _output != null; 

        if (_super) // Only one super-dim per table can exist
        {
            if (_input != null && _input.getSuperColumn() != null)
            {
                _input.getSuperColumn().remove(); // Replace the existing column by the new one
            }
        }

        if (_output != null) _output.getInputColumns().add(this);
        if (_input != null) _input.getColumns().add(this);
	}

	@Override
	public void remove() {
		assert _input != null && _output != null; 

        if (_output != null) _output.getInputColumns().remove(this);
        if (_input != null) _input.getColumns().remove(this);
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
	// ComColumnData interface
	//

	//
	// ComColumnDefinition interface
	//

	public static ComColumnData CreateColumnData(ComTable type, ComColumn column)
    {
        ComColumnData colData = new DimEmpty();

        if (type == null || String.IsNullOrEmpty(type.getName()))
        {
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Void"))
        {
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Top"))
        {
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Bottom")) // Not possible by definition
        {
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Root"))
        {
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Integer"))
        {
            colData = new DimPrimitive<Integer>(column);
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Double"))
        {
            colData = new DimPrimitive<Double>(column);
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Decimal"))
        {
            colData = new DimPrimitive<Decimal>(column);
        }
        else if (StringSimilarity.SameTableName(type.getName(), "String"))
        {
            colData = new DimPrimitive<String>(column);
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Boolean"))
        {
            colData = new DimPrimitive<Boolean>(column);
        }
        else if (StringSimilarity.SameTableName(type.getName(), "DateTime"))
        {
            colData = new DimPrimitive<LocalDate>(column);
        }
        else if (StringSimilarity.SameTableName(type.getName(), "Set"))
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
		this._key = isKey;
		this._super = isSuper;

		ComDataType dataType = output.getDataType();
        if(dataType == ComDataType.Void) {
        }
        else if(dataType == ComDataType.Top) {
        }
        else if(dataType == ComDataType.Bottom) {
        }
        else if(dataType == ComDataType.Root) {
        	_data = new DimPrimitive<Integer>(this, dataType);
        }
        else if(dataType == ComDataType.Integer) {
        	_data = new DimPrimitive<Integer>(this, dataType);
        }
        else if(dataType == ComDataType.Double) {
        	_data = new DimPrimitive<Double>(this, dataType);
        }
        else if(dataType == ComDataType.Decimal) {
        	_data = new DimPrimitive<BigDecimal>(this, dataType);
        }
        else if(dataType == ComDataType.String) {
        	_data = new DimPrimitive<String>(this, dataType);
        }
        else if(dataType == ComDataType.Boolean) {
        	_data = new DimPrimitive<Boolean>(this, dataType);
        }
        else if(dataType == ComDataType.DateTime) {
        	_data = new DimPrimitive<Instant>(this, dataType);
        }
		
        _definition = this;
	}
	
}
