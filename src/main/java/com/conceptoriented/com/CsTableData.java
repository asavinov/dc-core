package com.conceptoriented.com;

public interface CsTableData {

	public int getLength();
	public void setLength(int length);

	//
	// Tuple methods for manual set population and data manipulations
	//

	//
	// Table definition and evaluation methods
	//
	
	// Filter predicate function (maybe represent it like normal column with definition and data?)
	// Projection function(s) if this set is populated by outputs from some function
	// Dependencies
	//public void populate(); // Populate all functions and set length
}
