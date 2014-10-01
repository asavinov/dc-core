package com.conceptoriented.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Set implements ComTable, ComTableData {
	
	//
	// CsTable interface
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

	protected ComDataType dataType;
	@Override
	public ComDataType getDataType() {
		return dataType;
	}
	
	protected List<ComColumn> greaterDims;
	@Override
	public List<ComColumn> getColumns() {
		return greaterDims;
	}
	@Override
	public ComColumn getSuperColumn() {
		Optional<ComColumn> ret = greaterDims.stream().filter(x -> x.isSuper()).findAny();
		return ret.isPresent() ? ret.get() : null;
	}
	@Override
	public ComSchema getSchema() {
		ComTable t = this;
		while(t.getSuperColumn() != null) t = t.getSuperColumn().getOutput();
		return (ComSchema)t;
	}
	@Override
	public List<ComColumn> getKeyColumns() {
		return greaterDims.stream().filter(x -> x.isKey() && !x.isSuper()).collect(Collectors.toList());
	}
	@Override
	public List<ComColumn> getNonkeyColumns() {
		return greaterDims.stream().filter(x -> !x.isKey()).collect(Collectors.toList());
	}

	protected List<ComColumn> lesserDims;
	@Override
	public List<ComColumn> getInputColumns() {
		return lesserDims;
	}
	@Override
	public List<ComColumn> getSubColumns() {
		return lesserDims.stream().filter(x -> x.isSuper()).collect(Collectors.toList());
	}

	@Override
	public ComColumn getColumn(String name) {
		Optional<ComColumn> ret = greaterDims.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get() : null;
	}
	@Override
	public ComTable getTable(String name) {
		Optional<ComColumn> ret = lesserDims.stream().filter(x -> x.isSuper() && x.getInput().getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get().getInput() : null;
	}
	@Override
	public ComTable findTable(String name) {
		if(getName().equalsIgnoreCase(name)) return this;

		for(ComColumn c : getSubColumns()) {
			ComTable t = c.getInput().findTable(name);
			if(t != null) return t;
		}
		return null;
	}

	@Override
	public ComTableData getData() {
		return this;
	}

	//
	// CsTableData
	//

	protected int _length;
	@Override
	public int getLength() {
		return _length;
	}

	@Override
	public void setLength(int length) {
		_length = length;
	}

	//
	// Constructors
	//
	
	public Set(String name, ComDataType dataType) {
		this.name = name;
		this.dataType = dataType;
		
		greaterDims = new ArrayList<ComColumn>();
		lesserDims = new ArrayList<ComColumn>();
	}

}
