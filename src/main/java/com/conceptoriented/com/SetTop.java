package com.conceptoriented.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SetTop extends Set implements ComSchema {

	//
	// ComSchema interface
	//
	
	@Override
	public ComTable getPrimitive(String dataType) {
		Optional<ComColumn> col = getSubColumns().stream().filter(x -> Utils.sameTableName(x.getInput().getName(), dataType)).findAny();
		return col.isPresent() ? col.get().getInput() : null;
	}

	@Override
	public ComTable getRoot() {
		return getPrimitive("Root");
	}

    // Table factory

	@Override
	public ComTable createTable(String name) {
		ComTable table = new Set(name);
		return table;
	}
	
	@Override
	public ComTable addTable(ComTable table, ComTable parent, String superName) {
        if (parent == null)
        {
            parent = getRoot();
        }
        if (Utils.isNullOrEmpty(superName))
        {
            superName = "Super";
        }

        Dim dim = new Dim(superName, table, parent, true, true);

        dim.add();

        return table;
	}

	@Override
	public void deleteTable(ComTable table) {
		List<ComColumn> toRemove; 
		toRemove = new ArrayList<ComColumn>(table.getInputColumns());  
        for (ComColumn col : toRemove) 
        {
            col.remove();
        }
        toRemove = new ArrayList<ComColumn>(table.getColumns());
        for (ComColumn col : toRemove)
        {
            col.remove();
        }
	}

	@Override
	public void renameTable(ComTable table, String newName) {
        tableRenamed(table, newName); // Rename with propagation
        table.setName(newName);
	}
	
    // Column factory
	
	@Override
	public ComColumn createColumn(String name, ComTable input, ComTable output, boolean isKey) {

		ComColumn dim = new Dim(name, input, output, isKey, false);

        return dim;
	}
	@Override
	public void deleteColumn(ComColumn column) {
        columnDeleted(column);
        column.remove();
	}
	@Override
	public void renameColumn(ComColumn column, String newName) {
        columnRenamed(column, newName); // Rename with propagation
        column.setName(newName);
	}
	
    protected void columnRenamed(ComColumn column, String newName) {
		throw new UnsupportedOperationException();
    }

    protected void tableRenamed(ComTable table, String newName) {
		throw new UnsupportedOperationException();
    }
	
    protected void columnDeleted(ComColumn column) {
		throw new UnsupportedOperationException();
    }
	
    protected void createDataTypes() // Create all primitive data types from some specification like Enum, List or XML
    {
        Set set;
        Dim dim;

        set = new Set("Root");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Integer");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Double");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Decimal");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("String");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Boolean");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("DateTime");
        dim = new Dim("Top", set, this, true, true);
        dim.add();
    }

	public SetTop() {
		this("");
	}

	public SetTop(String name) {
		super(name);
		
        createDataTypes(); // Generate all predefined primitive sets as subsets
	}

}
