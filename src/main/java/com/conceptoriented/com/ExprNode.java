package com.conceptoriented.com;

import java.util.List;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

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
        if (getOperation() == OperationType.VALUE)
        {
        	boolean success;
            int intValue = 0;
            double doubleValue = 0.0;

            //
            // Resolve string into object and store in the result. Derive the type from the format. 
            //
            success = true;
            try { intValue = Integer.parseInt(getName()); } catch (NumberFormatException e) { success = false; }
            if (success)
            {
                getResult().setTypeName("Integer");
                getResult().setValue(intValue);
            }
            else {
                success = true;
                try { doubleValue = Double.parseDouble(getName()); } catch (NumberFormatException e) { success = false; }
                if (success)
                {
                	getResult().setTypeName("Double");
                	getResult().setValue(doubleValue);
                }
                else // Cannot parse means string
                {
                	getResult().setTypeName("String");
                	getResult().setValue(getName());
                }
            }
            getResult().setTypeTable(schema.getPrimitive(getResult().getTypeName()));
        }
        else if (getOperation() == OperationType.TUPLE)
        {
            //
            // Resolve this (assuming the parents are resolved)
            //
            if (Utils.isNullOrEmpty(getResult().getTypeName()))
            {
                ExprNode parentNode = (ExprNode)parent;
                if (parentNode == null)
                {
                    ;
                }
                else if (parentNode.getOperation() == OperationType.TUPLE) // Tuple in another tuple
                {
                    if (parentNode.getResult().getTypeTable() != null && !Utils.isNullOrEmpty(getName()))
                    {
                        ComColumn col = parentNode.getResult().getTypeTable().getColumn(getName());
                        setColumn(col);
                        getResult().setTypeTable(col.getOutput());
                        getResult().setTypeName(col.getOutput().getName());
                    }
                }
                else // Tuple in some other node, e.g, argument or value
                {
                    if (parentNode.getResult().getTypeTable() != null && !Utils.isNullOrEmpty(getName()))
                    {
                        ComColumn col = parentNode.getResult().getTypeTable().getColumn(getName());
                        setColumn(col);
                        getResult().setTypeTable(col.getOutput());
                        getResult().setTypeName(col.getOutput().getName());
                    }
                }
            }
            else if (getResult().getTypeTable() == null || !Utils.sameTableName(getResult().getTypeTable().getName(), getResult().getTypeName()))
            {
                // There is name without table, so we need to resolve this table name but against correct schema
            	throw new UnsupportedOperationException("TODO");
            }

            //
            // Resolve children (important: after the tuple itself, because this node will be used)
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.data.resolve(schema, variables);
            }
        }
        else if (getOperation() == OperationType.CALL)
        {
            //
            // Resolve children (important: before this node because this node uses children)
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.data.resolve(schema, variables);
            }
            
            // Resolve type name
            if (!Utils.isNullOrEmpty(getResult().getTypeName()))
            {
                getResult().setTypeTable(schema.getSubTable(getResult().getTypeName()));
            }

            //
            // Resolve this (assuming the children have been resolved)
            //
            ExprNode methodChild = getChild("method"); // Get column name
            ExprNode thisChild = getChild("this"); // Get column lesser set
            int childCount = children.size();

            if (childCount == 0) // Resolve variable (or add a child this variable assuming that it has been omitted)
            {
                // Try to resolve as a variable (including this variable). If success then finish.
            	Optional<ComVariable> varOpt = variables.stream().filter(v -> Utils.sameColumnName(v.getName(), getName())).findAny();

                if (varOpt != null) // Resolved as a variable
                {
                	ComVariable var = varOpt.get();
                    setVariable(var);

                    getResult().setTypeName(var.getTypeName());
                    getResult().setTypeTable(var.getTypeTable());
                }
                else // Cannot resolve as a variable - try resolve as a column name starting from 'this' table and then continue to super tables
                {
                    //
                    // Start from 'this' node bound to 'this' variable
                    //
                	Optional<ComVariable> thisVarOpt = variables.stream().filter(v -> Utils.sameColumnName(v.getName(), "this")).findAny();
                	ComVariable thisVar = thisVarOpt.get();

                    thisChild = new ExprNode();
                    thisChild.setOperation(OperationType.CALL);
                    thisChild.setAction(ActionType.READ);
                    thisChild.setName("this");
                    thisChild.getResult().setTypeName(thisVar.getTypeName());
                    thisChild.getResult().setTypeTable(thisVar.getTypeTable());
                    thisChild.setVariable(thisVar);

                    ExprNode path = thisChild;
                    ComTable contextTable = thisChild.getResult().getTypeTable();
                    ComColumn col = null;

                    while (contextTable != null)
                    {
                        //
                        // Try to resolve name
                        //
                        col = contextTable.getColumn(getName());

                        if (col != null) // Resolved
                        {
                            break;
                        }

                        //
                        // Iterator. Find super-column in the current context (where we have just failed to resolve the name)
                        //
                        ComColumn superColumn = contextTable.getSuperColumn();
                        contextTable = contextTable.getSuperTable();

                        if (contextTable == null || contextTable == contextTable.getSchema().getRoot())
                        {
                            break; // Root. No super dimensions anymore
                        }

                        //
                        // Build next super-access node and resolve it
                        //
                        ExprNode superNode = new ExprNode();
                        superNode.setOperation(OperationType.CALL);
                        superNode.setAction(ActionType.READ);
                        superNode.setName(superColumn.getName());
                        superNode.setColumn(superColumn);

                        superNode.addChild(path);
                        path = superNode;
                    }

                    if (col != null) // Successfully resolved. Store the results.
                    {
                        setColumn(col);

                        getResult().setTypeName(col.getOutput().getName());
                        getResult().setTypeTable(col.getOutput());

                        addChild(path);
                    }
                    else // Failed to resolve symbol
                    {
                        ; // ERROR: failed to resolve symbol in this and parent contexts
                    }
                }
            }
            else if (childCount == 1) // Function applied to previous output (resolve column)
            {
                String methodName = this.getName();
                ExprNode outputChild = null;
                if (thisChild != null) // Function applied to 'this' (resolve column)
                {
                    outputChild = thisChild;
                }
                else // Function applied to previous function output (resolve column)
                {
                    outputChild = getChild(0);
                }
                ComColumn col = outputChild.getResult().getTypeTable().getColumn(methodName);
                setColumn(col);

                getResult().setTypeName(col.getOutput().getName());
                getResult().setTypeTable(col.getOutput());
            }
            else // System procedure or operator (arithmetic, logical etc.)
            {
                String methodName = this.getName();

                // TODO: Derive return type. It is derived from arguments by using type conversion rules
                getResult().setTypeName("Double");
                getResult().setTypeTable(schema.getPrimitive(getResult().getTypeName()));

                switch (getAction())
                {
                    case MUL:
                    case DIV:
                    case ADD:
                    case SUB:
                        break;
                    default: // Some procedure. Find its API specification or retrieve via reflection
                        break;
                }
            }
        }
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
