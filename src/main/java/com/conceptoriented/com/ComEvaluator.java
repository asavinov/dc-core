package com.conceptoriented.com;

interface ComEvaluator {
    public boolean isUpdate();

    public boolean next(); // True if there exists a next element
    public boolean first(); // True if there exists a first element (if the set is not empty)
    public boolean last(); // True if there exists a last element (if the set is not empty)

    public Object evaluate(); // Compute output for the specified intput and write it
    public Object evaluateUpdate(); // Read group and measure for the specified input and compute update according to the aggregation formula. It may also increment another function if necessary.
    public boolean evaluateJoin(Object output); // Called for all pairs of input and output *if* the definition is a join predicate.

    public Object getResult();
}
