package com.conceptoriented.com;

import java.math.BigDecimal;
import java.time.Instant;

public class ComDim implements CsColumn, CsColumnDefinition {

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

	protected CsTable input;
	@Override
	public CsTable getInput() {
		return input;
	}
	@Override
	public void setInput(CsTable input) {
		this.input = input;
	}

	protected CsTable output;
	@Override
	public CsTable getOutput() {
		return output;
	}
	@Override
	public void setOutput(CsTable output) {
		this.output = output;
	}

	@Override
	public void add() {
		assert input != null && output != null; 

		((ComSet)input).greaterDims.add(this);
		((ComSet)output).lesserDims.add(this);
	}

	@Override
	public void remove() {
		assert input != null && output != null; 

		((ComSet)input).greaterDims.remove(this);
		((ComSet)output).lesserDims.remove(this);
	}

	protected CsColumnData columnData;
	@Override
	public CsColumnData getColumnData() {
		return columnData;
	}

	protected CsColumnDefinition columnDefinition;
	@Override
	public CsColumnDefinition getColumnDefinition() {
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
		CsColumn column = this;

		CsRecordEvaluator recordEvaluator = getEvaluator(); // Compile formula into computing object
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
        	CsTable loopTable = null; // TODO
    		for (int input = 0; input < loopTable.getTableData().getLength(); input++)
            {
            	recordEvaluator.evaluateUpdate(input);
            }
        }
        else {
        	 // Loop through all inputs of this set and *setting* (writing) the function output
    		CsTable loopTable = column.getInput();
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
	public CsRecordEvaluator getEvaluator() {
		CsColumn column = this;
		CsColumnData columnData = column.getColumnData();
		
		// TODO: Depending on the format of the formula and type of function instantiate a specific object

		return null;
	}

	//
	// Constructors
	//
	public ComDim(String name, CsTable input, CsTable output, boolean isKey, boolean isSuper) {
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
        	columnData = new ComColumnData<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Integer) {
        	columnData = new ComColumnData<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Double) {
        	columnData = new ComColumnData<Double>(this, dataType);
        }
        else if(dataType == CsDataType.Decimal) {
        	columnData = new ComColumnData<BigDecimal>(this, dataType);
        }
        else if(dataType == CsDataType.String) {
        	columnData = new ComColumnData<String>(this, dataType);
        }
        else if(dataType == CsDataType.Boolean) {
        	columnData = new ComColumnData<Boolean>(this, dataType);
        }
        else if(dataType == CsDataType.DateTime) {
        	columnData = new ComColumnData<Instant>(this, dataType);
        }
		
        columnDefinition = this;
	}
	
}
