package com.conceptoriented.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComSet implements CsTable, CsTableData {
	
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

	protected CsDataType dataType;
	@Override
	public CsDataType getDataType() {
		return dataType;
	}
	
	protected List<CsColumn> greaterDims;
	@Override
	public List<CsColumn> getOutputColumns() {
		return greaterDims;
	}
	@Override
	public CsColumn getSuperColumn() {
		Optional<CsColumn> ret = greaterDims.stream().filter(x -> x.isSuper()).findAny();
		return ret.isPresent() ? ret.get() : null;
	}
	@Override
	public CsSchema getSchema() {
		CsTable t = this;
		while(t.getSuperColumn() != null) t = t.getSuperColumn().getOutput();
		return (CsSchema)t;
	}
	@Override
	public List<CsColumn> getKeyColumns() {
		return greaterDims.stream().filter(x -> x.isKey() && !x.isSuper()).collect(Collectors.toList());
	}
	@Override
	public List<CsColumn> getNonkeyColumns() {
		return greaterDims.stream().filter(x -> !x.isKey()).collect(Collectors.toList());
	}

	protected List<CsColumn> lesserDims;
	@Override
	public List<CsColumn> getInputColumns() {
		return lesserDims;
	}
	@Override
	public List<CsColumn> getSubColumns() {
		return lesserDims.stream().filter(x -> x.isSuper()).collect(Collectors.toList());
	}

	@Override
	public CsColumn getColumn(String name) {
		Optional<CsColumn> ret = greaterDims.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get() : null;
	}
	@Override
	public CsTable getTable(String name) {
		Optional<CsColumn> ret = lesserDims.stream().filter(x -> x.isSuper() && x.getInput().getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get().getInput() : null;
	}
	@Override
	public CsTable findTable(String name) {
		if(getName().equalsIgnoreCase(name)) return this;

		for(CsColumn c : getSubColumns()) {
			CsTable t = c.getInput().findTable(name);
			if(t != null) return t;
		}
		return null;
	}

	@Override
	public CsTableData getTableData() {
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
	
	public ComSet(String name, CsDataType dataType) {
		this.name = name;
		this.dataType = dataType;
		
		greaterDims = new ArrayList<CsColumn>();
		lesserDims = new ArrayList<CsColumn>();
	}

}
