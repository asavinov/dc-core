package com.conceptoriented.com;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ExprNode extends TreeNode<ExprNode> {

    public ExprNode getChild(int child) { return (ExprNode)children.get(child); }
    public ExprNode getChild(String name)
    {
    	Optional<TreeNode<ExprNode>> child = children.stream().filter(x -> ((ExprNode)x).getName().equals(name)).findAny();
    	return child != null ? child.get().data : null;
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
        //
        // Evaluate children so that we have all their return values
        //

        if (getOperation() == OperationType.VALUE)
        {

        }
        else if (getOperation() == OperationType.TUPLE)
        {
            //
            // Evaluate children
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.data.evaluate();
            }

            if (getResult().getTypeTable().isPrimitive()) // Primitive TUPLE nodes are processed differently
            {
                ExprNode childNode = getChild(0);
                Object val = childNode.getResult().getValue();
                String targeTypeName = getResult().getTypeTable().getName();

                // Copy result from the child expression and convert it to this node type
                if (val instanceof String && Utils.isNullOrEmpty((String)val)) 
                {
                    getResult().setValue(null);
                }
                else if (Utils.sameTableName(targeTypeName, "Integer"))
                {
                    getResult().setValue((int)val);
                }
                else if (Utils.sameTableName(targeTypeName, "Double"))
                {
                    getResult().setValue((double)val);
                }
                else if(Utils.sameTableName(targeTypeName, "Decimal"))
                {
                    getResult().setValue(new BigDecimal(val.toString()));
                }
                else if (Utils.sameTableName(targeTypeName, "String"))
                {
                    getResult().setValue(val.toString());
                }
                else if (Utils.sameTableName(targeTypeName, "Boolean"))
                {
                    getResult().setValue((boolean)val);
                }
                else if (Utils.sameTableName(targeTypeName, "DateTime"))
                {
                    getResult().setValue(Instant.parse(val.toString()));
                }
                else
                {
                    throw new UnsupportedOperationException();
                }

                // Do execute the action because it is a primitive set
            }
            else // Non-primitive/non-leaf TUPLE node is a complex value with a special operation
            {
                // Find, append or update an element in this set (depending on the action type)
                if (getAction() == ActionType.READ) // Find the offset
                {
                    int input = getResult().getTypeTable().getData().find(this);

                    if (input < 0 || input >= getResult().getTypeTable().getData().getLength()) // Not found
                    {
                        getResult().setValue(null);
                    }
                    else
                    {
                        getResult().setValue(input);
                    }
                }
                else if (getAction() == ActionType.UPDATE) // Find and update the record
                {
                }
                else if (getAction() == ActionType.APPEND) // Find, try to update and append if cannot be found
                {
                    int input = getResult().getTypeTable().getData().find(this); // Uniqueness constraint: check if it exists already

                    if (input < 0 || input >= getResult().getTypeTable().getData().getLength()) // Not found
                    {
                        input = getResult().getTypeTable().getData().append(this); // Append new
                    }

                    getResult().setValue(input);
                }
                else
                {
                    throw new UnsupportedOperationException("ERROR: Other actions with tuples are not possible.");
                }
            }
        }
        else if (getOperation() == OperationType.CALL)
        {
            //
            // Evaluate children
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.data.evaluate();
            }

            int intRes;
            double doubleRes;
            boolean boolRes = false;

            if (getAction() == ActionType.READ)
            {
            	/*
                if (this instanceof CsvExprNode) // It is easier to do it here rather than (correctly) in the extension
                {
                    // Find current Row object
                    ExprNode thisNode = getChild("this");
                    String[] input = (String[])thisNode.getResult().getValue();

                    // Use attribute name or number by applying it to the current Row object (offset is not used)
                    int attributeIndex = ((DimCsv)getColumn()).ColumnIndex;
                    Object output = input[attributeIndex];
                    getResult().setValue(output);
                }
                else if (this instanceof OledbExprNode) // It is easier to do it here rather than (correctly) in the extension
                {
                    // Find current Row object
                    ExprNode thisNode = getChild("this");
                    DataRow input = (DataRow)thisNode.getResult().getValue();

                    // Use attribute name or number by applying it to the current Row object (offset is not used)
                    String attributeName = getName();
                    Object output = input[attributeName];
                    getResult().setValue(output);
                }
                else 
                */
            	if (getColumn() != null) 
                {
                    ExprNode prevOutput = getChild(0);
                    int input = (int)prevOutput.getResult().getValue();
                    Object output = getColumn().getData().getValue(input);
                    getResult().setValue(output);
                }
                else if (getVariable() != null)
                {
                    Object result = getVariable().getValue();
                    getResult().setValue(result);
                }
            }
            else if (getAction() == ActionType.UPDATE) // Compute new value for the specified offset using a new value in the variable
            {
            }
            //
            // MUL, DIV, ADD, SUB, 
            //
            else if (getAction() == ActionType.MUL)
            {
                doubleRes = 1.0;
                for (TreeNode<ExprNode> childNode : children)
                {
                    double arg = (double)childNode.data.getResult().getValue();
                    if (Double.isNaN(arg)) continue;
                    doubleRes *= arg;
                }
                getResult().setValue(doubleRes);
            }
            else if (getAction() == ActionType.DIV)
            {
                doubleRes = (double)((ExprNode)children.get(0)).getResult().getValue();
                for (int i = 1; i < children.size(); i++)
                {
                    double arg = (double)((ExprNode)children.get(i)).getResult().getValue();
                    if (Double.isNaN(arg)) continue;
                    doubleRes /= arg;
                }
                getResult().setValue(doubleRes);
            }
            else if (getAction() == ActionType.ADD)
            {
                doubleRes = 0.0;
                for (TreeNode<ExprNode> childNode : children)
                {
                    double arg = (double)childNode.data.getResult().getValue();
                    if (Double.isNaN(arg)) continue;
                    doubleRes += arg;
                }
                getResult().setValue(doubleRes);
            }
            else if (getAction() == ActionType.SUB)
            {
                doubleRes = (double)((ExprNode)children.get(0)).getResult().getValue();
                for (int i = 1; i < children.size(); i++)
                {
                    double arg = (double)((ExprNode)children.get(0)).getResult().getValue();
                    if (Double.isNaN(arg)) continue;
                    doubleRes /= arg;
                }
                getResult().setValue(doubleRes);
            }
            else if (getAction() == ActionType.COUNT)
            {
                intRes = (int)(((ExprNode)children.get(0)).getResult().getValue());
                intRes += 1;
                getResult().setValue(intRes);
            }
            //
            // LEQ, GEQ, GRE, LES,
            //
            else if (getAction() == ActionType.LEQ)
            {
                double arg1 = (double)((ExprNode)children.get(0)).getResult().getValue();
                double arg2 = (double)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 <= arg2;
                getResult().setValue(boolRes);
            }
            else if (getAction() == ActionType.GEQ)
            {
                double arg1 = (double)((ExprNode)children.get(0)).getResult().getValue();
                double arg2 = (double)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 >= arg2;
                getResult().setValue(boolRes);
            }
            else if (getAction() == ActionType.GRE)
            {
                double arg1 = (double)((ExprNode)children.get(0)).getResult().getValue();
                double arg2 = (double)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 > arg2;
                getResult().setValue(boolRes);
            }
            else if (getAction() == ActionType.LES)
            {
                double arg1 = (double)((ExprNode)children.get(0)).getResult().getValue();
                double arg2 = (double)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 < arg2;
                getResult().setValue(boolRes);
            }
            //
            // EQ, NEQ
            //
            else if (getAction() == ActionType.EQ)
            {
                double arg1 = (double)((ExprNode)children.get(0)).getResult().getValue();
                double arg2 = (double)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 == arg2;
                getResult().setValue(boolRes);
            }
            else if (getAction() == ActionType.NEQ)
            {
                double arg1 = (double)((ExprNode)children.get(0)).getResult().getValue();
                double arg2 = (double)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 != arg2;
                getResult().setValue(boolRes);
            }
            //
            // AND, OR
            //
            else if (getAction() == ActionType.AND)
            {
                boolean arg1 = (boolean)((ExprNode)children.get(0)).getResult().getValue();
                boolean arg2 = (boolean)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 && arg2;
                getResult().setValue(boolRes);
            }
            else if (getAction() == ActionType.OR)
            {
                boolean arg1 = (boolean)((ExprNode)children.get(0)).getResult().getValue();
                boolean arg2 = (boolean)((ExprNode)children.get(1)).getResult().getValue();
                boolRes = arg1 || arg2;
                getResult().setValue(boolRes);
            }
            else // Some procedure. Find its API specification or retrieve via reflection
            {
            }
        }
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
