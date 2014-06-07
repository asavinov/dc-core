package com.conceptoriented.com;

import java.util.Optional;

public class ComTop extends ComSet implements CsSchema {

	@Override
	public CsTable getPrimitive(CsDataType dataType) {
		Optional<CsColumn> col = getSubColumns().stream().filter(x -> x.getInput().getDataType() == dataType).findAny();
		return col.isPresent() ? col.get().getInput() : null;
	}

	@Override
	public CsTable getRoot() {
		return getPrimitive(CsDataType.Root);
	}

	@Override
	public CsTable createTable(String name, CsDataType dataType, CsTable parent) {
		if(parent == null) {
			parent = getRoot();
		}

		CsTable table = new ComSet(name, dataType);
		CsColumn superCol = new ComDim("Super", table, parent, true, true);
		superCol.add();
		return table;
	}

	protected CsTable createPrimitiveTable(CsDataType dataType) {
		CsTable table = null;
		CsColumn superCol = null;

		if(dataType == CsDataType.Root) {
			table = new ComSet("Top", dataType);
			superCol = new ComDim("Super", table, this, true, true);
		}
		else if(dataType == CsDataType.Integer) {
			table = new ComSet("Integer", dataType);
			superCol = new ComDim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.Double) {
			table = new ComSet("Double", dataType);
			superCol = new ComDim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.Decimal) {
			table = new ComSet("Decimal", dataType);
			superCol = new ComDim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.String) {
			table = new ComSet("String", dataType);
			superCol = new ComDim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.Boolean) {
			table = new ComSet("Boolean", dataType);
			superCol = new ComDim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.DateTime) {
			table = new ComSet("DateTime", dataType);
			superCol = new ComDim("Top", table, this, true, true);
		}
		
		if(superCol != null) {
			superCol.add();
			return table;
		}
		else {
			return null;
		}
	}

	public ComTop(String name) {
		super(name, CsDataType.Top);
		
		//
		// Instantiate all primitive sets
		//
		createPrimitiveTable(CsDataType.Root);
		createPrimitiveTable(CsDataType.Integer);
		createPrimitiveTable(CsDataType.Double);
		createPrimitiveTable(CsDataType.Decimal);
		createPrimitiveTable(CsDataType.String);
		createPrimitiveTable(CsDataType.Boolean);
		createPrimitiveTable(CsDataType.DateTime);
	}

}
