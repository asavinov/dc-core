package com.conceptoriented.com;

import java.util.Optional;

public class SetTop extends Set implements ComSchema {

	@Override
	public ComTable getPrimitive(ComDataType dataType) {
		Optional<ComColumn> col = getSubColumns().stream().filter(x -> x.getInput().getDataType() == dataType).findAny();
		return col.isPresent() ? col.get().getInput() : null;
	}

	@Override
	public ComTable getRoot() {
		return getPrimitive(ComDataType.Root);
	}

	@Override
	public ComTable createTable(String name, ComDataType dataType, ComTable parent) {
		if(parent == null) {
			parent = getRoot();
		}

		ComTable table = new Set(name, dataType);
		ComColumn superCol = new Dim("Super", table, parent, true, true);
		superCol.add();
		return table;
	}

	protected ComTable createPrimitiveTable(ComDataType dataType) {
		ComTable table = null;
		ComColumn superCol = null;

		if(dataType == ComDataType.Root) {
			table = new Set("Top", dataType);
			superCol = new Dim("Super", table, this, true, true);
		}
		else if(dataType == ComDataType.Integer) {
			table = new Set("Integer", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == ComDataType.Double) {
			table = new Set("Double", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == ComDataType.Decimal) {
			table = new Set("Decimal", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == ComDataType.String) {
			table = new Set("String", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == ComDataType.Boolean) {
			table = new Set("Boolean", dataType);
			superCol = new Dim("Top", table, this, true, true);
		}
		else if(dataType == ComDataType.DateTime) {
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
		super(name, ComDataType.Top);
		
		//
		// Instantiate all primitive sets
		//
		createPrimitiveTable(ComDataType.Root);
		createPrimitiveTable(ComDataType.Integer);
		createPrimitiveTable(ComDataType.Double);
		createPrimitiveTable(ComDataType.Decimal);
		createPrimitiveTable(ComDataType.String);
		createPrimitiveTable(ComDataType.Boolean);
		createPrimitiveTable(ComDataType.DateTime);
	}

}
