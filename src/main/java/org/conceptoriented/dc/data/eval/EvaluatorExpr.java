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

package org.conceptoriented.dc.data.eval;

import java.util.ArrayList;
import java.util.Arrays;

import org.conceptoriented.dc.schema.*;
import org.conceptoriented.dc.data.*;

public class EvaluatorExpr implements DcEvaluator {
    protected DcColumnData columnData;

    // Loop
    protected int thisCurrent;
    protected DcTable thisTable;
    protected DcVariable thisVariable; // Stores current input (offset in a local set or reference to the current DataRow)

    // Output expression
    protected ExprNode outputExpr; // Can contain more specific nodes OledbExprNode to access attributes in DataRow

    //
    // ComColumnEvaluator interface
    //

    protected DcWorkspace workspace;
    @Override
    public DcWorkspace getWorkspace() { return workspace; }
    @Override
    public void setWorkspace(DcWorkspace workspace) { this.workspace = workspace; }

    @Override
    public boolean nextInput() {
        if (thisCurrent < thisTable.getData().getLength()) thisCurrent++;

        if (thisCurrent < thisTable.getData().getLength()) return true;
        else return false;
    }

    @Override
    public boolean firstInput() {
        thisCurrent = 0;

        if (thisCurrent < thisTable.getData().getLength()) return true;
        else return false;
    }

    @Override
    public boolean lastInput() {
        thisCurrent = thisTable.getData().getLength() - 1;

        if (thisCurrent >= 0) return true;
        else return false;
    }

    @Override
    public Object evaluate() {
        // Use input value to evaluate the expression
        thisVariable.setValue(thisCurrent);

        // evaluate the expression
        outputExpr.evaluate();

        // Write the result value to the function
        if (columnData != null)
        {
            columnData.setValue(thisCurrent, outputExpr.getOutputVariable().getValue());
        }

        return outputExpr.getOutputVariable().getValue();
    }

    @Override
    public Object getOutput() {
        return outputExpr.getOutputVariable().getValue();
    }

    public EvaluatorExpr(DcColumn column)
    {
        setWorkspace(column.getInput().getSchema().getWorkspace());
        columnData = column.getData();

        // Loop
        thisCurrent = -1;
        thisTable = column.getInput();
        thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
        thisVariable.setTypeSchema(thisTable.getSchema());
        thisVariable.setTypeTable(thisTable);

        // Output expression
        if (column.getDefinition().getFormulaExpr() != null)
        {
            outputExpr = column.getDefinition().getFormulaExpr();

            if (outputExpr.getDefinitionType() == ColumnDefinitionType.LINK)
            {
                // Adjust the expression according to other parameters of the definition
                if(column.getDefinition().isAppendData()) {
                    outputExpr.setAction(ActionType.APPEND);
                }
                else
                {
                    outputExpr.setAction(ActionType.READ);
                }
            }
        }

        outputExpr.getOutputVariable().setSchemaName(column.getOutput().getSchema().getName());
        outputExpr.getOutputVariable().setTypeName(column.getOutput().getName());
        outputExpr.getOutputVariable().setTypeSchema(column.getOutput().getSchema());
        outputExpr.getOutputVariable().setTypeTable(column.getOutput());

        outputExpr.resolve(workspace, new ArrayList<DcVariable>(Arrays.asList(thisVariable)));
    }

    public EvaluatorExpr(DcTable table)
    {
        setWorkspace(table.getSchema().getWorkspace());
        columnData = null;

        // Loop
        thisCurrent = -1;
        thisTable = table;
        thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");

        thisVariable.setTypeSchema(thisTable.getSchema());
        thisVariable.setTypeTable(thisTable);

        // Outtput expression
        outputExpr = table.getDefinition().getWhereExpr();
        outputExpr.resolve(workspace, Arrays.asList(thisVariable));
    }

    public EvaluatorExpr()
    {
    }
}

