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

package com.conceptoriented.dce;

import java.util.ArrayList;
import java.util.Arrays;

public class ExprEvaluator implements ComEvaluator {
    protected ComColumnData columnData;

    // Loop
    protected int thisCurrent;
    protected ComTable thisTable;
    protected ComVariable thisVariable; // Stores current input (offset in a local set or reference to the current DataRow)

    // Output expression
    protected ExprNode outputExpr; // Can contain more specific nodes OledbExprNode to access attributes in DataRow

    //
    // ComColumnEvaluator interface
    //

    protected Workspace workspace;
    @Override
    public Workspace getWorkspace() { return workspace; }
    @Override
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    @Override
    public boolean next() {
        if (thisCurrent < thisTable.getData().getLength()) thisCurrent++;

        if (thisCurrent < thisTable.getData().getLength()) return true;
        else return false;
    }

    @Override
    public boolean first() {
        thisCurrent = 0;

        if (thisCurrent < thisTable.getData().getLength()) return true;
        else return false;
    }

    @Override
    public boolean last() {
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
            columnData.setValue(thisCurrent, outputExpr.getResult().getValue());
        }

        return outputExpr.getResult().getValue();
    }

    @Override
    public Object getResult() {
        return outputExpr.getResult().getValue();
    }

    public ExprEvaluator(ComColumn column)
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
        if (column.getDefinition().getMapping() != null)
        {
            if (column.getDefinition().isAppendData())
            {
                outputExpr = column.getDefinition().getMapping().BuildExpression(ActionType.APPEND);
            }
            else
            {
                outputExpr = column.getDefinition().getMapping().BuildExpression(ActionType.READ);
            }
        }
        else if (column.getDefinition().getFormulaExpr() != null)
        {
            outputExpr = column.getDefinition().getFormulaExpr();

            if (column.getDefinition().getDefinitionType() == ColumnDefinitionType.LINK)
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

        outputExpr.getResult().setSchemaName(column.getOutput().getSchema().getName());
        outputExpr.getResult().setTypeName(column.getOutput().getName());
        outputExpr.getResult().setTypeSchema(column.getOutput().getSchema());
        outputExpr.getResult().setTypeTable(column.getOutput());

        outputExpr.resolve(workspace, new ArrayList<ComVariable>(Arrays.asList(thisVariable)));
    }

    public ExprEvaluator(ComTable table)
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

    public ExprEvaluator()
    {
    }
}


class AggrEvaluator extends ExprEvaluator
{
    // base::columnData is the aggregated function to be computed

    // Facts
    // base::thisCurrent is offset in the fact table
    // base::thisTable is a fact set which is iterated in this class
    // base::thisVariable stores current fact in the loop table. is used by group expr and meausre expr

    // Groups
    protected ComVariable groupVariable; // Stores current group (input for the aggregated function)
    protected ExprNode groupExpr; // Returns a group this fact belongs to, is stored in the group variable

    // Measure
    protected ComVariable measureVariable; // Stores new value (output for the aggregated function)
    protected ExprNode measureExpr; // Returns a new value to be aggregated with the old value, is stored in the measure variable

    // Updater/aggregation function
    // base::outputExpr - updater expression. works in the context of two variables: group and measure

    //
    // ComColumnEvaluator interface
    //
    @Override
    public Object evaluate()
    {
        //
        // Evalute group and measure expressions for the current fact
        //

        // Use input value to evaluate the expression
        thisVariable.setValue(thisCurrent);

        groupExpr.evaluate();
        int groupElement = (int)groupExpr.getResult().getValue();
        groupVariable.setValue(groupElement);

        measureExpr.evaluate();
        Object measureValue = measureExpr.getResult().getValue();
        measureVariable.setValue(measureValue);

        //
        // Evaluate the update expression and store the new computed value
        //
        outputExpr.evaluate();

        Object newValue = outputExpr.getResult().getValue();
        columnData.setValue(groupElement, newValue);

        return outputExpr.getResult().getValue();
    }

    public AggrEvaluator(ComColumn column) // Create evaluator from structured definition
    {
        setWorkspace(column.getInput().getSchema().getWorkspace());
        columnData = column.getData();

        if (column.getDefinition().getFormulaExpr() == null) // From structured definition (parameters)
        {
            // Facts
            thisCurrent = -1;
            thisTable = column.getDefinition().getFactTable();

            thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
            thisVariable.setTypeSchema(thisTable.getSchema());
            thisVariable.setTypeTable(thisTable);

            // Groups
            groupExpr = ExprNode.createReader(column.getDefinition().getGroupPaths().get(0), true); // Currently only one path is used
            groupExpr = (ExprNode)groupExpr.getRoot();
            groupExpr.resolve(workspace, Arrays.asList(thisVariable));

            groupVariable = new Variable(column.getInput().getSchema().getName(), column.getInput().getName(), "this");
            groupVariable.setTypeSchema(column.getInput().getSchema());
            groupVariable.setTypeTable(column.getInput());

            // Measure
            measureExpr = ExprNode.createReader(column.getDefinition().getMeasurePaths().get(0), true);
            measureExpr = (ExprNode)measureExpr.getRoot();
            measureExpr.resolve(workspace, Arrays.asList(thisVariable));

            measureVariable = new Variable(column.getOutput().getSchema().getName(), column.getOutput().getName(), "value");
            measureVariable.setTypeSchema(column.getOutput().getSchema());
            measureVariable.setTypeTable(column.getOutput());

            // Updater/aggregation function
            outputExpr = ExprNode.createUpdater(column, column.getDefinition().getUpdater());
            outputExpr.resolve(workspace, Arrays.asList(groupVariable, measureVariable));
        }
        else // From expression
        {
            //
            // Extract all aggregation components from expression (aggregation expression cannot be resolved)
            //
            ExprNode aggExpr = column.getDefinition().getFormulaExpr();

            // Facts
            ExprNode factsNode = aggExpr.getChild("facts").getChild(0);
            String thisTableName = factsNode.getName();

            thisCurrent = -1;
            thisTable = column.getInput().getSchema().getSubTable(thisTableName);

            thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
            thisVariable.setTypeSchema(thisTable.getSchema());
            thisVariable.setTypeTable(thisTable);

            // Groups
            ExprNode groupsNode = aggExpr.getChild("groups").getChild(0);
            groupExpr = groupsNode;
            groupExpr.resolve(workspace, Arrays.asList(thisVariable));

            groupVariable = new Variable(column.getInput().getSchema().getName(), column.getInput().getName(), "this");
            groupVariable.setTypeSchema(column.getInput().getSchema());
            groupVariable.setTypeTable(column.getInput());

            // Measure
            ExprNode measureNode = aggExpr.getChild("measure").getChild(0);
            measureExpr = measureNode;
            measureExpr.resolve(workspace, Arrays.asList(thisVariable));

            measureVariable = new Variable(column.getOutput().getSchema().getName(), column.getOutput().getName(), "value");
            measureVariable.setTypeSchema(column.getOutput().getSchema());
            measureVariable.setTypeTable(column.getOutput());

            // Updater/aggregation function
            ExprNode updaterExpr = aggExpr.getChild("aggregator").getChild(0);

            outputExpr = ExprNode.createUpdater(column, updaterExpr.getName());
            outputExpr.resolve(workspace, Arrays.asList(groupVariable, measureVariable));
        }
    }

}
