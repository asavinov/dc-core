package com.conceptoriented.com;

/**
 * Describes and computes one function in terms of other functions. 
 * 
 * @author savinov
 *
 */
public interface CsColumnDefinition {

	// The form of representation:
	// Our own v-expr or its parsed AST: 
	// Native source code: Java, C# etc.
	// Native library class: Java, C#, Python etc.
	// OS script (e.g., using pipes): Bash, Python etc.
	
	// Type of formula:
	// Primitive (direct computations returning a primitive value): = [col 1]+this.[col 2] / SYS_FUNC([col 3].[col 4] - 1.0)
	// Complex (mapping, tuple): ([Set 1] [s 1] = this.[a1], [Set 2] [s 2] = (...), [Double] [amount] = [col1]+[col2] )
	// A sequence of statements with return (primitive or tuple).
	// Aggregation/accumulation (loop over another set): standard (sum, mul etc. - separate class), user-defined like this+value/2 - 1.
	// Join predicate (two loops over input and output sets)

	public CsColumnEvaluator getColumnEvaluator(); // Get an object which is used to compute the function values according to the formula
	
	//
	// Dependencies. Either manual or automatic.
	//

	//
	// Function computation. Organize a loop and use a computation procedure for each input.
	//
	
	public void initialize();
	public void evaluate();
	public void finish(); // Can be used for computing the final aggregated function using the second count column
	
}

/**
 * Compute function output for the specified input. It is used for computing and storing function outputs. 
 * An implementation is produced by the column definition and based on the definition. 
 * It can be viewed as a procedure implementing (compiling) the definition.
 * It reads some column data and writes some column data according to the formula.     
 * 
 * @author savinov
 *
 */
interface CsColumnEvaluator {

	public Object evaluateSet(int input); // Compute output for the specified input and write it 
	public Object evaluateUpdate(int input); // Read group and measure for the specified input and compute update according to the aggregation formula. It may also increment another function if necessary.
	public boolean evaluateJoin(int input, Object output);  // Called for all pairs of input and output *if* the definition is a join predicate. Set the output if it is true.

}
