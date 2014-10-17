package com.conceptoriented.com;

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
	public Object evaluateUpdate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean evaluateJoin(Object output) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getResult() {
		return outputExpr.getResult().getValue();
	}

    public ExprEvaluator(ComColumn column)
    {
        columnData = column.getData();

        // Loop
        thisCurrent = -1;
        thisTable = column.getInput();
        thisVariable = new Variable("this", thisTable.getName());
        thisVariable.setTypeTable(thisTable);

        // Output expression
        if (column.getDefinition().getMapping() != null)
        {
            if (column.getDefinition().isGenerating())
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
        }

        outputExpr.getResult().setTypeName(column.getOutput().getName());
        outputExpr.getResult().setTypeTable(column.getOutput());

        outputExpr.resolve(column.getInput().getSchema(), new ArrayList<ComVariable>(Arrays.asList(thisVariable)));
    }

    public ExprEvaluator(ComTable table)
    {
        columnData = null;

        // Loop
        thisCurrent = -1;
        thisTable = table;
        thisVariable = new Variable("this", thisTable.getName());
        thisVariable.setTypeTable(thisTable);

        // Outtput expression
        outputExpr = table.getDefinition().getWhereExpr();
        outputExpr.resolve(thisTable.getSchema(), Arrays.asList(thisVariable));
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
        columnData = column.getData();

        if (column.getDefinition().getFormulaExpr() == null) // From structured definition (parameters)
        {
            // Facts
            thisCurrent = -1;
            thisTable = column.getDefinition().getFactTable();

            thisVariable = new Variable("this", thisTable.getName());
            thisVariable.setTypeTable(thisTable);

            // Groups
            groupExpr = ExprNode.createReader(column.getDefinition().getGroupPaths().get(0), true); // Currently only one path is used
            groupExpr = (ExprNode)groupExpr.getRoot();
            groupExpr.resolve(thisTable.getSchema(), Arrays.asList(thisVariable));

            groupVariable = new Variable("this", column.getInput().getName());
            groupVariable.setTypeTable(column.getInput());

            // Measure
            measureExpr = ExprNode.createReader(column.getDefinition().getMeasurePaths().get(0), true);
            measureExpr = (ExprNode)measureExpr.getRoot();
            measureExpr.resolve(thisTable.getSchema(), Arrays.asList(thisVariable));

            measureVariable = new Variable("value", column.getOutput().getName());
            measureVariable.setTypeTable(column.getOutput());

            // Updater/aggregation function
            outputExpr = ExprNode.createUpdater(column, column.getDefinition().getUpdater());
            outputExpr.resolve(column.getInput().getSchema(), Arrays.asList(groupVariable, measureVariable));
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

            thisVariable = new Variable("this", thisTable.getName());
            thisVariable.setTypeTable(thisTable);

            // Groups
            ExprNode groupsNode = aggExpr.getChild("groups").getChild(0);
            groupExpr = groupsNode;
            groupExpr.resolve(thisTable.getSchema(), Arrays.asList(thisVariable));

            groupVariable = new Variable("this", column.getInput().getName());
            groupVariable.setTypeTable(column.getInput());

            // Measure
            ExprNode measureNode = aggExpr.getChild("measure").getChild(0);
            measureExpr = measureNode;
            measureExpr.resolve(thisTable.getSchema(), Arrays.asList(thisVariable));

            measureVariable = new Variable("value", column.getOutput().getName());
            measureVariable.setTypeTable(column.getOutput());

            // Updater/aggregation function
            ExprNode updaterExpr = aggExpr.getChild("aggregator").getChild(0);

            outputExpr = ExprNode.createUpdater(column, updaterExpr.getName());
            outputExpr.resolve(column.getInput().getSchema(), Arrays.asList(groupVariable, measureVariable));
        }
    }

}
