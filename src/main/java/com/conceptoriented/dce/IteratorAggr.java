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

import java.util.Arrays;

class IteratorAggr extends IteratorExpr
{
    // base::columnData is the aggregated function to be computed

    // Facts
    // base::thisCurrent is offset in the fact table
    // base::thisTable is a fact set which is iterated in this class
    // base::thisVariable stores current fact in the loop table. is used by group expr and meausre expr

    // Groups
    protected DcVariable groupVariable; // Stores current group (input for the aggregated function)
    protected ExprNode groupExpr; // Returns a group this fact belongs to, is stored in the group variable

    // Measure
    protected DcVariable measureVariable; // Stores new value (output for the aggregated function)
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

    public IteratorAggr(DcColumn column) // Create evaluator from structured definition
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