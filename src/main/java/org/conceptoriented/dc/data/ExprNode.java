 /*
 * Copyright 2013-2015 Alexandr Savinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.conceptoriented.dc.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import org.conceptoriented.dc.utils.*;
import org.conceptoriented.dc.schema.*;
import org.conceptoriented.dc.data.*;

public class ExprNode extends TreeNode<ExprNode> {

    public ExprNode getChild(int child) { return (ExprNode)children.get(child).item; }
    public ExprNode getChild(String name)
    {
        Optional<TreeNode<ExprNode>> child = children.stream().filter(x -> x.item.getName().equals(name)).findAny();
        return child.isPresent() ? child.get().item : null;
    }

    protected OperationType _operation;
    public OperationType getOperation() {
        return _operation;
    }
    public void setOperation(OperationType value) {
        _operation = value;
    }

    protected String _nameSpace;
    public String getNameSpace() {
        return _nameSpace;
    }
    public void setNameSpace(String value) {
        this._nameSpace = value;
    }

    protected String _name;
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        this._name = name;
    }

    protected DcColumn _column;
    public DcColumn getColumn() {
        return _column;
    }
    public void setColumn(DcColumn column) {
        this._column = column;
    }

    public DcVariable _variable;
    public DcVariable getVariable() {
        return _variable;
    }
    public void setVariable(DcVariable variable) {
        this._variable = variable;
    }

    protected Method _method;
    public Method getMethod() {
        return _method;
    }
    public void setMethod(Method value) {
        this._method = value;
    }

    protected ActionType _action;
    public ActionType getAction() {
        return _action;
    }
    public void setAction(ActionType value) {
        this._action = value;
    }

    protected DcTableWriter _tableWriter;
    public DcTableWriter getTableWriter() {
        return _tableWriter;
    }
    public void setTableWriter(DcTableWriter value) {
        this._tableWriter = value;
    }

    public ColumnDefinitionType getDefinitionType() {
        if(getOperation() == OperationType.TUPLE) {
            return ColumnDefinitionType.LINK;
        }
        else if(getOperation() == OperationType.CALL && getName().equalsIgnoreCase("AGGREGATE")) {
            return ColumnDefinitionType.AGGREGATION;
        }
        else {
            return ColumnDefinitionType.ARITHMETIC;
        }
    }

    public DcVariable _outputVariable;
    public DcVariable getOutputVariable() {
        return _outputVariable;
    }
    public void setOutputVariable(DcVariable variable) {
        this._outputVariable = variable;
    }

    public void evaluateAndResolveSchema(DcSpace workspace, List<DcVariable> variables) {
        if (getOperation() == OperationType.VALUE)
        {
            boolean success;
            int intValue = 0;
            double doubleValue = 0.0;

            getOutputVariable().resolve(workspace);

            //
            // Resolve string into object and store in the result. Derive the type from the format.
            //
            success = true;
            try { intValue = Integer.parseInt(getName()); } catch (NumberFormatException e) { success = false; }
            if (success)
            {
                getOutputVariable().setTypeName("Integer");
                getOutputVariable().setValue(intValue);
            }
            else {
                success = true;
                try { doubleValue = Double.parseDouble(getName()); } catch (NumberFormatException e) { success = false; }
                if (success)
                {
                    getOutputVariable().setTypeName("Double");
                    getOutputVariable().setValue(doubleValue);
                }
                else // Cannot parse means string
                {
                    getOutputVariable().setTypeName("String");
                    getOutputVariable().setValue(getName());
                }
            }
        }
        else if (getOperation() == OperationType.TUPLE)
        {
            //
            // Resolve this (tuples are resolved through the parent which must be resolved before children)
            // In TUPLE, Name denotes a function from the parent (input) to this node (output)
            //

            //
            // Resolve type table name
            //
            getOutputVariable().resolve(workspace);

            //
            // Resolve Name into a column object (a function from the parent to this node)
            //
            ExprNode parentNode = (ExprNode)parent;
            if (parentNode == null)
            {
                ;
            }
            else if (parentNode.getOperation() == OperationType.TUPLE) // This tuple in another tuple
            {
                if (parentNode.getOutputVariable().getTypeTable() != null && !Utils.isNullOrEmpty(getName()))
                {
                    DcColumn col = parentNode.getOutputVariable().getTypeTable().getColumn(getName());

                    if (col != null) // Column resolved
                    {
                        // Check and process type information
                        if (getOutputVariable().getTypeTable() == null)
                        {
                            getOutputVariable().setSchemaName(col.getOutput().getSchema().getName());
                            getOutputVariable().setTypeName(col.getOutput().getName());
                            getOutputVariable().setTypeSchema(col.getOutput().getSchema());
                            getOutputVariable().setTypeTable(col.getOutput());
                        }
                        else if (getOutputVariable().getTypeTable() != col.getOutput())
                        {
                            ; // ERROR: Output type of the column must be the same as this node result type
                        }
                    }
                    else // Column not found
                    {
                        // Append a new column (schema change, e.g., if function output structure has to be propagated)
                        DcTable input = parentNode.getOutputVariable().getTypeTable();
                        DcTable output = getOutputVariable().getTypeTable();
                        String columnName = getName();

                        col = input.getSchema().createColumn(columnName, input, output, false);
                        col.add();
                    }

                    setColumn(col);
                }
            }
            else // This tuple in some other node, e.g, argument or value
            {
                ; // Is it a valid situation?
            }

            //
            // Resolve children (important: after the tuple itself, because this node will be used)
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.item.evaluateAndResolveSchema(workspace, variables);
            }
        }
        else if (getOperation() == OperationType.CALL)
        {
            //
            // Resolve children (important: before this node because this node uses children)
            // In CALL, Name denotes a function from children (input) to this node (output)
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.item.evaluateAndResolveSchema(workspace, variables);
            }

            //
            // Resolve type table name
            //
            getOutputVariable().resolve(workspace);

            //
            // Resolve Name into a column object, variable, procedure or whatever object that will return a result (children must be resolved before)
            //
            ExprNode methodChild = getChild("method"); // Get column name
            ExprNode thisChild = getChild("this"); // Get column lesser set
            int childCount = children.size();

            if( !Utils.isNullOrEmpty(getNameSpace()) ) // External name space (java call, c# call etc.)
            {
                String className = getNameSpace().trim();
                if(getNameSpace().startsWith("call:"))
                {
                    className = className.substring(5).trim();
                }
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                String methodName = getName();
                Method[]  methods = null;
                methods = clazz.getMethods();
                for(Method m : methods) {
                    if(!m.getName().equals(methodName)) continue;
                    if(Modifier.isStatic(m.getModifiers())) {
                        if(m.getParameterCount() != childCount) continue;
                    }
                    else {
                        if(m.getParameterCount() + 1 != childCount) continue;
                    }

                    setMethod(m);
                    break;
                }
            }
            else if (childCount == 0) // Resolve variable (or add a child this variable assuming that it has been omitted)
            {
                // Try to resolve as a variable (including this variable). If success then finish.
                Optional<DcVariable> varOpt = variables.stream().filter(v -> Utils.sameColumnName(v.getName(), getName())).findAny();

                if (varOpt.isPresent()) // Resolved as a variable
                {
                    DcVariable var = varOpt.get();

                    getOutputVariable().setSchemaName(var.getSchemaName());
                    getOutputVariable().setTypeName(var.getTypeName());
                    getOutputVariable().setTypeSchema(var.getTypeSchema());
                    getOutputVariable().setTypeTable(var.getTypeTable());

                    setVariable(var);
                }
                else // Cannot resolve as a variable - try resolve as a column name starting from 'this' table and then continue to super tables
                {
                    //
                    // Start from 'this' node bound to 'this' variable
                    //
                    Optional<DcVariable> thisVarOpt = variables.stream().filter(v -> Utils.sameColumnName(v.getName(), "this")).findAny();
                    DcVariable thisVar = thisVarOpt.get();

                    thisChild = new ExprNode();
                    thisChild.setOperation(OperationType.CALL);
                    thisChild.setAction(ActionType.READ);
                    thisChild.setName("this");

                    thisChild.getOutputVariable().setSchemaName(thisVar.getSchemaName());
                    thisChild.getOutputVariable().setTypeName(thisVar.getTypeName());
                    thisChild.getOutputVariable().setTypeSchema(thisVar.getTypeSchema());
                    thisChild.getOutputVariable().setTypeTable(thisVar.getTypeTable());

                    thisChild.setVariable(thisVar);

                    ExprNode path = thisChild;
                    DcTable contextTable = thisChild.getOutputVariable().getTypeTable();
                    DcColumn col = null;

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
                        DcColumn superColumn = contextTable.getSuperColumn();
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

                    if (col != null) // Column resolved
                    {
                        setColumn(col);

                        // Check and process type information
                        if (getOutputVariable().getTypeTable() == null)
                        {
                            getOutputVariable().setSchemaName(col.getOutput().getSchema().getName());
                            getOutputVariable().setTypeName(col.getOutput().getName());
                            getOutputVariable().setTypeSchema(col.getOutput().getSchema());
                            getOutputVariable().setTypeTable(col.getOutput());
                        }
                        else if (getOutputVariable().getTypeTable() != col.getOutput())
                        {
                            ; // ERROR: Output type of the column must be the same as this node result type
                        }

                        addChild(path);
                    }
                    else // Column not found
                    {
                        ; // ERROR: failed to resolve symbol
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

                DcColumn col = outputChild.getOutputVariable().getTypeTable().getColumn(methodName);

                if (col != null) // Column resolved
                {
                    setColumn(col);

                    // Check and process type information
                    if (getOutputVariable().getTypeTable() == null)
                    {
                        getOutputVariable().setSchemaName(col.getOutput().getSchema().getName());
                        getOutputVariable().setTypeName(col.getOutput().getName());
                        getOutputVariable().setTypeSchema(col.getOutput().getSchema());
                        getOutputVariable().setTypeTable(col.getOutput());
                    }
                    else if (getOutputVariable().getTypeTable() != col.getOutput())
                    {
                        ; // ERROR: Output type of the column must be the same as this node result type
                    }
                }
                else // Column not found
                {
                    ; // ERROR: failed to resolve symbol
                }
            }
            else // System procedure or operator (arithmetic, logical etc.)
            {
                String methodName = this.getName();

                // TODO: Derive return type. It is derived from arguments by using type conversion rules
                getOutputVariable().setTypeName("Double");
                getOutputVariable().resolve(workspace);

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

    public void evaluateBegin()
    {
        if (getOperation() == OperationType.VALUE)
        {

        }
        else if (getOperation() == OperationType.TUPLE) // SET, TABLE, NON-PRIMITIVE, ...
        {
            // Open file for writing
            if (getOutputVariable().getTypeTable() != null && !getOutputVariable().getTypeTable().isPrimitive() && !getOutputVariable().getTypeTable().isPrimitive())
            {
                setTableWriter(getOutputVariable().getTypeTable().getData().getTableWriter());
                getTableWriter().open();
            }

            //
            // Evaluate children
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.item.evaluateBegin();
            }
        }
        else if (getOperation() == OperationType.CALL) // FUNCTION, COLUMN, PRIMITIVE, ...
        {
            //
            // Evaluate children
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.item.evaluateBegin();
            }
        }
    }

    public void evaluateEnd()
    {
        if (getOperation() == OperationType.VALUE)
        {

        }
        else if (getOperation() == OperationType.TUPLE) // SET, TABLE, NON-PRIMITIVE, ...
        {
            // Close file
            if (getTableWriter() != null) getTableWriter().close();

            //
            // Evaluate children
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.item.evaluateEnd();
            }
        }
        else if (getOperation() == OperationType.CALL) // FUNCTION, COLUMN, PRIMITIVE, ...
        {
            //
            // Evaluate children
            //
            for (TreeNode<ExprNode> childNode : children)
            {
                childNode.item.evaluateEnd();
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
                childNode.item.evaluate();
            }

            if (getOutputVariable().getTypeTable().isPrimitive()) // Primitive TUPLE nodes are processed differently
            {
                ExprNode childNode = getChild(0);
                Object val = childNode.getOutputVariable().getValue();
                String targeTypeName = getOutputVariable().getTypeTable().getName();

                // Copy result from the child expression and convert it to this node type
                if (val instanceof String && Utils.isNullOrEmpty((String)val))
                {
                    getOutputVariable().setValue(null);
                }
                else if (Utils.sameTableName(targeTypeName, "Integer"))
                {
                    getOutputVariable().setValue(Utils.toInt32(val));
                }
                else if (Utils.sameTableName(targeTypeName, "Double"))
                {
                    getOutputVariable().setValue(Utils.toDouble(val));
                }
                else if(Utils.sameTableName(targeTypeName, "Decimal"))
                {
                    getOutputVariable().setValue(Utils.toDecimal(val));
                }
                else if (Utils.sameTableName(targeTypeName, "String"))
                {
                    getOutputVariable().setValue(val.toString());
                }
                else if (Utils.sameTableName(targeTypeName, "Boolean"))
                {
                    getOutputVariable().setValue(Utils.toBoolean(val));
                }
                else if (Utils.sameTableName(targeTypeName, "DateTime"))
                {
                    getOutputVariable().setValue(Utils.toDateTime(val));
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
                    int input = getTableWriter().find(this);

                    if (input < 0 || input >= getOutputVariable().getTypeTable().getData().getLength()) // Not found
                    {
                        getOutputVariable().setValue(null);
                    }
                    else
                    {
                        getOutputVariable().setValue(input);
                    }
                }
                else if (getAction() == ActionType.UPDATE) // Find and update the record
                {
                }
                else if (getAction() == ActionType.APPEND) // Find, try to update and append if cannot be found
                {
                    int input = getTableWriter().find(this); // Uniqueness constraint: check if it exists already

                    if (input < 0 || input >= getOutputVariable().getTypeTable().getData().getLength()) // Not found
                    {
                        input = getTableWriter().append(this); // Append new
                    }

                    getOutputVariable().setValue(input);
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
                childNode.item.evaluate();
            }

            int intRes;
            double doubleRes;
            boolean boolRes = false;
            Object objRes = null;

            int childCount = children.size();

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
                    int input = (int)prevOutput.getOutputVariable().getValue();
                    Object output = getColumn().getData().getValue(input);
                    getOutputVariable().setValue(output);
                }
                else if (getVariable() != null)
                {
                    Object result = getVariable().getValue();
                    getOutputVariable().setValue(result);
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
                    double arg = Utils.toDouble(childNode.item.getOutputVariable().getValue());
                    if (Double.isNaN(arg)) continue;
                    doubleRes *= arg;
                }
                getOutputVariable().setValue(doubleRes);
            }
            else if (getAction() == ActionType.DIV)
            {
                doubleRes = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                for (int i = 1; i < children.size(); i++)
                {
                    double arg = Utils.toDouble(((ExprNode)children.get(i)).getOutputVariable().getValue());
                    if (Double.isNaN(arg)) continue;
                    doubleRes /= arg;
                }
                getOutputVariable().setValue(doubleRes);
            }
            else if (getAction() == ActionType.ADD)
            {
                doubleRes = 0.0;
                for (TreeNode<ExprNode> childNode : children)
                {
                    double arg = Utils.toDouble(childNode.item.getOutputVariable().getValue());
                    if (Double.isNaN(arg)) continue;
                    doubleRes += arg;
                }
                getOutputVariable().setValue(doubleRes);
            }
            else if (getAction() == ActionType.SUB)
            {
                doubleRes = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                for (int i = 1; i < children.size(); i++)
                {
                    double arg = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                    if (Double.isNaN(arg)) continue;
                    doubleRes /= arg;
                }
                getOutputVariable().setValue(doubleRes);
            }
            else if (getAction() == ActionType.COUNT)
            {
                intRes = Utils.toInt32((((ExprNode)children.get(0)).getOutputVariable().getValue()));
                intRes += 1;
                getOutputVariable().setValue(intRes);
            }
            //
            // LEQ, GEQ, GRE, LES,
            //
            else if (getAction() == ActionType.LEQ)
            {
                double arg1 = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                double arg2 = Utils.toDouble(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 <= arg2;
                getOutputVariable().setValue(boolRes);
            }
            else if (getAction() == ActionType.GEQ)
            {
                double arg1 = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                double arg2 = Utils.toDouble(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 >= arg2;
                getOutputVariable().setValue(boolRes);
            }
            else if (getAction() == ActionType.GRE)
            {
                double arg1 = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                double arg2 = Utils.toDouble(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 > arg2;
                getOutputVariable().setValue(boolRes);
            }
            else if (getAction() == ActionType.LES)
            {
                double arg1 = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                double arg2 = Utils.toDouble(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 < arg2;
                getOutputVariable().setValue(boolRes);
            }
            //
            // EQ, NEQ
            //
            else if (getAction() == ActionType.EQ)
            {
                double arg1 = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                double arg2 = Utils.toDouble(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 == arg2;
                getOutputVariable().setValue(boolRes);
            }
            else if (getAction() == ActionType.NEQ)
            {
                double arg1 = Utils.toDouble(((ExprNode)children.get(0)).getOutputVariable().getValue());
                double arg2 = Utils.toDouble(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 != arg2;
                getOutputVariable().setValue(boolRes);
            }
            //
            // AND, OR
            //
            else if (getAction() == ActionType.AND)
            {
                boolean arg1 = Utils.toBoolean(((ExprNode)children.get(0)).getOutputVariable().getValue());
                boolean arg2 = Utils.toBoolean(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 && arg2;
                getOutputVariable().setValue(boolRes);
            }
            else if (getAction() == ActionType.OR)
            {
                boolean arg1 = Utils.toBoolean(((ExprNode)children.get(0)).getOutputVariable().getValue());
                boolean arg2 = Utils.toBoolean(((ExprNode)children.get(1)).getOutputVariable().getValue());
                boolRes = arg1 || arg2;
                getOutputVariable().setValue(boolRes);
            }
            else if (getAction() == ActionType.PROCEDURE)
            {
                Class<?>[] types = getMethod().getParameterTypes();

                Object thisObj = null;
                Object[] args = null;

                // Preparing parameters for the procedure
                if(Modifier.isStatic(getMethod().getModifiers())) {
                    args = new Object[childCount];
                    for(int i=0; i<childCount; i++) {
                        args[i] = ((ExprNode)children.get(i)).getOutputVariable().getValue();
                    }
                }
                else {
                    if(childCount > 0) thisObj = ((ExprNode)children.get(0)).getOutputVariable().getValue();

                    args = new Object[childCount - 1];
                    for(int i=0; i<childCount-1; i++) {
                        args[i] = ((ExprNode)children.get(i+1)).getOutputVariable().getValue();
                    }
                }

                // Dynamic invocation
                try {
                    objRes = getMethod().invoke(thisObj, args);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                getOutputVariable().setValue(objRes);
            }
            else // Some procedure. Find its API specification or retrieve via reflection
            {
            }
        }
    }


    public static ExprNode createReader(ColumnPath path, boolean withThisVariable)
    {
        ExprNode expr = null;

        if(false)
        {

        }
        /*
        if (path.Input.Schema is SetTopCsv) // Access via column index
        {
            throw new UnsupportedOperationException();
        }
        else if (path.Input.Schema is SetTopOledb) // Access via relational attribute
        {
            throw new UnsupportedOperationException();
        }
        */
        else // Access via function/column composition
        {
            for (int i = path.getSegments().size() - 1; i >= 0; i--)
            {
                DcColumn seg = path.getSegments().get(i);

                ExprNode node = new ExprNode();
                node.setOperation(OperationType.CALL);
                node.setAction(ActionType.READ);
                node.setName(seg.getName());

                node.getOutputVariable().setSchemaName(seg.getOutput().getSchema().getName());
                node.getOutputVariable().setTypeName(seg.getOutput().getName());
                node.getOutputVariable().setTypeSchema(seg.getOutput().getSchema());
                node.getOutputVariable().setTypeTable(seg.getOutput());

                if (expr != null)
                {
                    expr.addChild(node);
                }

                expr = node;
            }
        }

        //
        // Create the last node corresponding to this variable and append it to the expression
        //
        ExprNode thisNode = null;
        if (withThisVariable)
        {
            thisNode = new ExprNode();
            thisNode.setName("this");
            thisNode.setOperation(OperationType.CALL);
            thisNode.setAction(ActionType.READ);

            thisNode.getOutputVariable().setSchemaName(path.getInput().getSchema().getName());
            thisNode.getOutputVariable().setTypeName(path.getInput().getName());
            thisNode.getOutputVariable().setTypeSchema(path.getInput().getSchema());
            thisNode.getOutputVariable().setTypeTable(path.getInput());

            if (expr != null)
            {
                expr.addChild(thisNode);
                expr = thisNode;
            }
        }

        return expr;
    }

    public static ExprNode createReader(DcColumn column, boolean withThisVariable)
    {
        return createReader(new ColumnPath(column), withThisVariable);
    }

    public static ExprNode createUpdater(DcColumn column, String aggregationFunction)
    {
        ActionType aggregation;
        if (aggregationFunction.equals("COUNT"))
        {
            aggregation = ActionType.COUNT;
        }
        else if (aggregationFunction.equals("SUM"))
        {
            aggregation = ActionType.ADD;
        }
        else if (aggregationFunction.equals("MUL"))
        {
            aggregation = ActionType.MUL;
        }
        else
        {
            throw new UnsupportedOperationException("Aggregation function is not implemented.");
        }

        //
        // A node for reading the current function value at the offset in 'this' variable
        //
        ExprNode currentValueNode = (ExprNode)createReader(column, true).getRoot();

        //
        // A node for reading a new function value from the well-known variable
        //
        ExprNode valueNode = new ExprNode();
        valueNode.setName("value");
        valueNode.setOperation(OperationType.CALL);
        valueNode.setAction(ActionType.READ);

        valueNode.getOutputVariable().setSchemaName(column.getOutput().getSchema().getName());
        valueNode.getOutputVariable().setTypeName(column.getOutput().getName());
        valueNode.getOutputVariable().setTypeSchema(column.getOutput().getSchema());
        valueNode.getOutputVariable().setTypeTable(column.getOutput());

        //
        // A node for computing a result (updated) function value from the current value and new value
        //
        ExprNode expr = new ExprNode();
        expr.setOperation(OperationType.CALL);
        expr.setAction(aggregation); // SUM etc.
        expr.setName(column.getName());

        expr.getOutputVariable().setSchemaName(column.getOutput().getSchema().getName());
        expr.getOutputVariable().setTypeName(column.getOutput().getName());
        expr.getOutputVariable().setTypeSchema(column.getOutput().getSchema());
        expr.getOutputVariable().setTypeTable(column.getOutput());

        // Two arguments in child nodes
        expr.addChild(currentValueNode);
        expr.addChild(valueNode);

        return expr;
    }

    public ExprNode()
    {
        setOutputVariable(new Variable("", "Void", "return"));
    }
}
