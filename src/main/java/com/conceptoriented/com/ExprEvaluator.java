package com.conceptoriented.com;

import java.util.ArrayList;
import java.util.Arrays;

public class ExprEvaluator implements ComEvaluator {

    protected ExprNode exprNode; // Can contain more specific nodes OledbExprNode to access attributes in DataRow

    protected ComVariable thisVariable; // Stores current input (offset in a local set or reference to the current DataRow)

    protected int currentElement;

    protected ComColumnData columnData;

    protected ComTable loopTable;

    //
    // ComColumnEvaluator interface
    //

	protected boolean isUpdate;
	@Override
	public boolean isUpdate() {
		return isUpdate;
	}
	
	@Override
	public boolean next() {
        if (currentElement < loopTable.getData().getLength()) currentElement++;

        if (currentElement < loopTable.getData().getLength()) return true;
        else return false;
	}

	@Override
	public boolean first() {
        currentElement = 0;

        if (currentElement < loopTable.getData().getLength()) return true;
        else return false;
	}

	@Override
	public boolean last() {
        currentElement = loopTable.getData().getLength() - 1;

        if (currentElement >= 0) return true;
        else return false;
	}

	@Override
	public Object evaluate() {
        // Use input value to evaluate the expression
        thisVariable.setValue(currentElement);

        // evaluate the expression
        exprNode.evaluate();

        // Write the result value to the function
        if (columnData != null)
        {
            columnData.setValue(currentElement, exprNode.getResult().getValue());
        }

        return exprNode.getResult().getValue();
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
		return exprNode.getResult().getValue();
	}

    public ExprEvaluator(ComColumn column)
    {
        if (column.getDefinition().getMapping() != null)
        {
            if (column.getDefinition().isGenerating())
            {
                exprNode = column.getDefinition().getMapping().BuildExpression(ActionType.APPEND);
            }
            else
            {
                exprNode = column.getDefinition().getMapping().BuildExpression(ActionType.READ);
            }
        }
        else if (column.getDefinition().getFormulaExpr() != null)
        {
            exprNode = column.getDefinition().getFormulaExpr();
        }

        currentElement = -1;
        loopTable = column.getInput();
        isUpdate = false;
        thisVariable = new Variable("this", loopTable.getName());
        thisVariable.setTypeTable(loopTable);
        columnData = column.getData();

        // Resolve names in the expresion by storing direct references to storage objects which will be used during valuation (names will not be used
        exprNode.resolve(column.getInput().getSchema(), new ArrayList<ComVariable>(Arrays.asList(thisVariable)));
    }

}
