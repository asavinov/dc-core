package com.conceptoriented.com;

import java.math.BigDecimal;
import java.time.Instant;

public class ComDim implements CsColumn {

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

	protected CsDataColumn dataColumn;
	@Override
	public CsDataColumn getDataColumn() {
		return dataColumn;
	}

	//
	// Constructors
	//
	public ComDim(String name, CsTable input, CsTable output, boolean isSuper, boolean isKey) {
		assert name != null && input != null && output != null;

		this.name = name;
		this.input = input;
		this.output = output;
		this.isSuper = isSuper;
		this.isKey = isKey;

		CsDataType dataType = output.getDataType();
        if(dataType == CsDataType.Void) {
        }
        else if(dataType == CsDataType.Top) {
        }
        else if(dataType == CsDataType.Bottom) {
        }
        else if(dataType == CsDataType.Root) {
        	dataColumn = new ComDataColumn<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Integer) {
        	dataColumn = new ComDataColumn<Integer>(this, dataType);
        }
        else if(dataType == CsDataType.Double) {
        	dataColumn = new ComDataColumn<Double>(this, dataType);
        }
        else if(dataType == CsDataType.Decimal) {
        	dataColumn = new ComDataColumn<BigDecimal>(this, dataType);
        }
        else if(dataType == CsDataType.String) {
        	dataColumn = new ComDataColumn<String>(this, dataType);
        }
        else if(dataType == CsDataType.Boolean) {
        	dataColumn = new ComDataColumn<Boolean>(this, dataType);
        }
        else if(dataType == CsDataType.DateTime) {
        	dataColumn = new ComDataColumn<Instant>(this, dataType);
        }
		
	}
	
}
