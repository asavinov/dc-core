package com.conceptoriented.com;

import java.util.Optional;

public class SetTop extends Set implements ComSchema {

	@Override
	public ComTable getPrimitive(CsDataType dataType) {
		Optional<ComColumn> col = getSubColumns().stream().filter(x -> x.getInput().getDataType() == dataType).findAny();
		return col.isPresent() ? col.get().getInput() : null;
	}

	@Override
	public ComTable getRoot() {
		return getPrimitive(CsDataType.Root);
	}

	@Override
	public ComTable createTable(String name, CsDataType dataType, ComTable parent) {
		if(parent == null) {
			parent = getRoot();
		}

		ComTable table = new Set(name, dataType);
		ComColumn superCol = new Dim("Super", table, parent, true, true);
		superCol.add();
		return table;
	}

	protected ComTable createPrimitiveTable(CsDataType dataType) {
		ComTable table = null;
		ComColumn superCol = null;

		if(dataType == CsDataType.Root) {
			table = new Set("Top", dataType);
			superCol = new Dim("Super", table, this, true, true);
		}
		else if(dataType == CsDataType.Integer) {
			table = new Set("Integer", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.Double) {
			table = new Set("Double", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.Decimal) {
			table = new Set("Decimal", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.String) {
			table = new Set("String", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.Boolean) {
			table = new Set("Boolean", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == CsDataType.DateTime) {
			table = new Set("DateTime", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		
		if(superCol != null) {
			superCol.add();
			return table;
		}
		else {
			return null;
		}
	}

	public SetTop(String name) {
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
