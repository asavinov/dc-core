package com.conceptoriented.com;

import java.math.BigDecimal;
import java.time.Instant;

public class Dim implements ComColumn, ComColumnDefinition {

	//
	// CsColumn interface
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

	protected boolean isSuper;
	@Override
	public boolean isSuper() {
		return isSuper;
	}

	protected boolean isKey;
	@Override
	public boolean isKey() {
		return isKey;
	}

	protected ComTable input;
	@Override
	public ComTable getInput() {
		return input;
	}
	@Override
	public void setInput(ComTable input) {
		this.input = input;
	}

	protected ComTable output;
	@Override
	public ComTable getOutput() {
		return output;
	}
	@Override
	public void setOutput(ComTable output) {
		this.output = output;
	}

	@Override
	public void add() {
		assert input != null && output != null; 

		((Set)input).greaterDims.add(this);
		((Set)output).lesserDims.add(this);
	}

	@Override
	public void remove() {
		assert input != null && output != null; 

		((Set)input).greaterDims.remove(this);
		((Set)output).lesserDims.remove(this);
	}

	protected ComColumnData columnData;
	@Override
	public ComColumnData getColumnData() {
		return columnData;
	}

	protected ComColumnDefinition columnDefinition;
	@Override
	public ComColumnDefinition getColumnDefinition() {
		return columnDefinition;
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

		CsColumnEvaluator recordEvaluator = getColumnEvaluator(); // Compile formula into computing object
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
	public CsColumnEvaluator getColumnEvaluator() {
		ComColumn column = this;
		ComColumnData columnData = column.getColumnData();
		
		// TODO: Depending on the format of the formula and type of function instantiate a specific object

		return null;
	}

	//
	// Constructors
	//
	public Dim(String name, ComTable input, ComTable output, boolean isKey, boolean isSuper) {
		assert name != null && input != null && output != null;

		this.name = name;
		this.input = input;
		this.output = output;
		this.isKey = isKey;
		this.isSuper = isSuper;

		CsDataType dataType = output.getDataType();
        if(dataType == CsDataType.Void) {
        }
        else if(dataType == CsDataType.Top) {
        }
        else if(dataType == CsDataType.Bottom) {
        }
        else if(dataType == CsDataType.Root) {
        	columnData = new DimPrimitive<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Integer) {
        	columnData = new DimPrimitive<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Double) {
        	columnData = new DimPrimitive<Double>(this, dataType);
        }
        else if(dataType == CsDataType.Decimal) {
        	columnData = new DimPrimitive<BigDecimal>(this, dataType);
        }
        else if(dataType == CsDataType.String) {
        	columnData = new DimPrimitive<String>(this, dataType);
        }
        else if(dataType == CsDataType.Boolean) {
        	columnData = new DimPrimitive<Boolean>(this, dataType);
        }
        else if(dataType == CsDataType.DateTime) {
        	columnData = new DimPrimitive<Instant>(this, dataType);
        }
		
        columnDefinition = this;
	}
	
}
