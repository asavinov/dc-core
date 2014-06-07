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

	public ComTop(String name) {
		super(name, CsDataType.Top);
		
		//
		// Instantiate all primitive sets
		//
		CsTable rootT = new ComSet("Root", CsDataType.Root);
		CsColumn rootC = new ComDim("Root", rootT, this, true, true);
		rootC.add();

		CsTable integerT = new ComSet("Integer", CsDataType.Integer);
		CsColumn integerC = new ComDim("Root", integerT, this, true, true);
		integerC.add();

		CsTable doubleT = new ComSet("Double", CsDataType.Double);
		CsColumn doubleC = new ComDim("Double", doubleT, this, true, true);
		doubleC.add();

		CsTable decimalT = new ComSet("Decimal", CsDataType.Decimal);
		CsColumn decimalC = new ComDim("Decimal", decimalT, this, true, true);
		decimalC.add();

		CsTable stringT = new ComSet("String", CsDataType.String);
		CsColumn stringC = new ComDim("String", stringT, this, true, true);
		stringC.add();

		CsTable booleanT = new ComSet("Boolean", CsDataType.Boolean);
		CsColumn booleanC = new ComDim("Boolean", booleanT, this, true, true);
		booleanC.add();

		CsTable dateTimeT = new ComSet("DateTime", CsDataType.DateTime);
		CsColumn dateTimeC = new ComDim("DateTime", dateTimeT, this, true, true);
		dateTimeC.add();
	}

}
