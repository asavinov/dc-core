package com.conceptoriented.com;

import java.util.List;

public class ExprNode extends TreeNode<ExprNode> {

    public ExprNode getChild(int child) { throw new UnsupportedOperationException("TODO"); }
    public ExprNode getChild(String name)
    {
    	throw new UnsupportedOperationException("TODO");
    }

	protected OperationType _operation;
    public OperationType getOperation() {
    	return _operation;
    }
    public void setOperation(OperationType value) {
    	_operation = value;
    }
    
	protected String _name;
	public String getName() {
		return _name;
	}
	public void setName(String name) {
		this._name = name;
	}
	
    protected ComColumn _column;
    public ComColumn getColumn() {
		return _column;
	}
	public void setColumn(ComColumn column) {
		this._column = column;
	}

	public ComVariable _variable;
    public ComVariable getVariable() {
		return _variable;
	}
	public void setVariable(ComVariable variable) {
		this._variable = variable;
	}

	protected ActionType _action;
	public ActionType getAction() {
		return _action;
	}
	public void setAction(ActionType value) {
		this._action = value;
	}

    public ComVariable _result;
	public ComVariable getResult() {
		return _result;
	}
	public void setResult(ComVariable result) {
		this._result = result;
	}


    public void resolve(ComSchema schema, List<ComVariable> variables) {
    	throw new UnsupportedOperationException("TODO");
    }

    public void evaluate() {
    	throw new UnsupportedOperationException("TODO");
    }
    
	

    
    public ExprNode()
    {
        setResult(new Variable("return", "Void"));
    }
}

enum OperationType
{
    // - (VALUE): do we need this? could be modeled by tuples with no children or by type. A primitive value (literal) represented here by-value
    VALUE, // this node stores a value. no computations

    // - TUPLE/DOWN/ROW_OP: access to the named function input given a combination of outputs.
    //   - name is a function from a (single) parent node set (input) to this node set (output) - but computed in inverse direction from this node to the parent when processing the parent (this node processes children's functions)
    //   - node type = input type of any child function (surrogate), child type(s) = output(s) type of the child function
    //   - TUPLE always means moving down in poset by propagating greater surrogates to lesser surrogates
    TUPLE, // children are tuple attributes corresponding to dimensions. this node de-projects their values and find the input for this node set

    // Operation type. What is in this node and how to interpret the name
    // - ACCESS/UP/COLUMN_OP: access to named function (variable, arithmetic, system procedure) output given intput(s) in children
    //   - name is a function from several children (inputs) to this node set (output) - computed when processing this node
    //   - node type = output type (surrogate), child type(s) = input(s) type
    //   - it means moving up in the poset from lesser values to greater values
    CALL, // this node processes children and produces output for this node. children are named arguments. the first argument is 'method'
}

enum ActionType
{
    // Variable or column or tuple
    READ, // Read accossor or getter. Read value from a variable/function or find a surrogate for a tuple. Normally used in method parameters.
    WRITE, // Assignment, write accessor or setter. Write value to an existing variable/function (do nothing if it does not exist). Normally is used for assignment. 
    UPDATE, // Update value by applying some operation. Normally is used for affecting directly a target rather than loading it, changing and then storing.
    APPEND, // Append a value if it does not exist and write it if does exist. The same as write except that a new element can be added
    INSERT, // The same as append except that a position is specified
    ALLOC, // For variables and functions as a whole storage object in the context. Is not it APPEND/INSERT?
    FREE,

    PROCEDURE, // Generic procedure call including system calls

    OPERATION, // Built-in operation like plus and minus

    // Unary
    NEG,
    NOT,

    // Arithmetics
    MUL,
    DIV,
    ADD,
    SUB,

    // Logic
    LEQ,
    GEQ,
    GRE,
    LES,

    EQ,
    NEQ,

    AND,
    OR,

    // Arithmetics
    COUNT,
    // ADD ("SUM")
    // MUL ("MUL")
}
