package com.conceptoriented.com;

interface ComEvaluator {
	public Workspace getWorkspace();
	public void setWorkspace(Workspace workspace);

	public boolean next(); // True if there exists a next element
    public boolean first(); // True if there exists a first element (if the set is not empty)
    public boolean last(); // True if there exists a last element (if the set is not empty)

    public Object evaluate(); // Compute output for the specified intput and write it

    public Object getResult();
}
