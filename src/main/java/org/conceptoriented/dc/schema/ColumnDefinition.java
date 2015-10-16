package org.conceptoriented.dc.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.conceptoriented.dc.utils.*;
import org.conceptoriented.dc.data.*;
import org.conceptoriented.dc.data.eval.*;
import org.conceptoriented.dc.data.query.*;

public class ColumnDefinition implements DcColumnDefinition
{
    protected DcColumn _dim;

    //
    // ComColumnDefinition interface
    //

    //
    // COEL (language) representation
    //

    protected String _formula;
    @Override
    public String getFormula() { return _formula; }
    @Override
    public void setFormula(String value)
    {
        _formula = value;
		
        if (Utils.isNullOrEmpty(value)) return;

        ExprBuilder exprBuilder = new ExprBuilder();
        ExprNode expr = exprBuilder.build(_formula);

        setFormulaExpr(expr);
    }

    //
    // Structured (object) representation
    //

    protected boolean _appendData;
    public boolean isAppendData() { return _appendData; }
    public void setAppendData(boolean value) { _appendData = value; }

    protected boolean _appendSchema;
    public boolean isAppendSchema() { return _appendSchema; }
    public void setAppendSchema(boolean value) { _appendSchema = value; }

    protected ExprNode _formulaExpr;
    @Override
    public ExprNode getFormulaExpr() { return _formulaExpr; }
    @Override
    public void setFormulaExpr(ExprNode value) { _formulaExpr = value; }

    //
    // Compute
    //

    @Override
    public void evaluate()
    {
        if (getFormulaExpr() == null || getFormulaExpr().getDefinitionType() == ColumnDefinitionType.FREE)
        {
            return; // Nothing to evaluate
        }
    	
        // General parameters
        DcWorkspace workspace = _dim.getInput().getSchema().getWorkspace();
        DcColumnData columnData = _dim.getData();

        _dim.getData().setAutoIndex(false);
        //_dim.getData().nullify();
    
        Object thisCurrent = null;
        if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.ARITHMETIC || getFormulaExpr().getDefinitionType() == ColumnDefinitionType.LINK)
        {
            if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.LINK)
            {
            	// Adjust the expression according to other parameters of the definition
                if(_dim.getDefinition().isAppendData()) 
                {
                	getFormulaExpr().setAction(ActionType.APPEND);
                }
                else
                {
                	getFormulaExpr().setAction(ActionType.READ);
                }
            }

            // Prepare parameter variables for the expression 
            DcTable thisTable = _dim.getInput();
            DcVariable thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
            thisVariable.setTypeSchema(thisTable.getSchema());
            thisVariable.setTypeTable(thisTable);
            
            // Parameterize expression and resolve it (bind names to real objects) 
            getFormulaExpr().getOutputVariable().setSchemaName(_dim.getOutput().getSchema().getName());
            getFormulaExpr().getOutputVariable().setTypeName(_dim.getOutput().getName());
            getFormulaExpr().getOutputVariable().setTypeSchema(_dim.getOutput().getSchema());
            getFormulaExpr().getOutputVariable().setTypeTable(_dim.getOutput());
            getFormulaExpr().resolve(workspace, new ArrayList<DcVariable>(Arrays.asList(thisVariable)));
            
        	getFormulaExpr().evaluateBegin();
            DcTableReader tableReader = thisTable.getTableReader();
        	tableReader.open();
            while ((thisCurrent = tableReader.next()) != null)
            {
                thisVariable.setValue(thisCurrent); // Set parameters of the expression

                getFormulaExpr().evaluate(); // Evaluate the expression

                if (columnData != null)
                {
                    Object newValue = getFormulaExpr().getOutputVariable().getValue();
                    columnData.setValue((int)thisCurrent, newValue);
                }
            }
        	tableReader.close();
            getFormulaExpr().evaluateEnd();
        }
        else if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.AGGREGATION)
        {
            // Facts
            ExprNode factsNode = getFormulaExpr().getChild("facts").getChild(0);

            // This table and variable
            String thisTableName = factsNode.getName();
            DcTable thisTable = _dim.getInput().getSchema().getSubTable(thisTableName);
            DcVariable thisVariable = new Variable(thisTable.getSchema().getName(), thisTable.getName(), "this");
            thisVariable.setTypeSchema(thisTable.getSchema());
            thisVariable.setTypeTable(thisTable);

            // Groups
            ExprNode groupExpr; // Returns a group this fact belongs to, is stored in the group variable
            ExprNode groupsNode = getFormulaExpr().getChild("groups").getChild(0);
            groupExpr = groupsNode;
            groupExpr.resolve(workspace, Arrays.asList(thisVariable));

            DcVariable groupVariable; // Stores current group (input for the aggregated function)
            groupVariable = new Variable(_dim.getInput().getSchema().getName(), _dim.getInput().getName(), "this");
            groupVariable.setTypeSchema(_dim.getInput().getSchema());
            groupVariable.setTypeTable(_dim.getInput());

            // Measure
            ExprNode measureExpr; // Returns a new value to be aggregated with the old value, is stored in the measure variable
            ExprNode measureNode = getFormulaExpr().getChild("measure").getChild(0);
            measureExpr = measureNode;
            measureExpr.resolve(workspace, Arrays.asList(thisVariable));

            DcVariable measureVariable; // Stores new value (output for the aggregated function)
            measureVariable = new Variable(_dim.getOutput().getSchema().getName(), _dim.getOutput().getName(), "value");
            measureVariable.setTypeSchema(_dim.getOutput().getSchema());
            measureVariable.setTypeTable(_dim.getOutput());

            // Updater/aggregation function
            ExprNode updaterExpr = getFormulaExpr().getChild("aggregator").getChild(0);

            ExprNode outputExpr;
            outputExpr = ExprNode.createUpdater(_dim, updaterExpr.getName());
            outputExpr.resolve(workspace, Arrays.asList(groupVariable, measureVariable));
        
            getFormulaExpr().evaluateBegin();
            DcTableReader tableReader = thisTable.getTableReader();
            tableReader.open();
            while ((thisCurrent = tableReader.next()) != null)
            {
            	thisVariable.setValue(thisCurrent); // Set parameters of the expression

                groupExpr.evaluate();
                int groupElement = (int)groupExpr.getOutputVariable().getValue();
                groupVariable.setValue(groupElement);

                measureExpr.evaluate();
                Object measureValue = measureExpr.getOutputVariable().getValue();
                measureVariable.setValue(measureValue);

                outputExpr.evaluate(); // Evaluate the expression

                // Write the result value to the function
                if (columnData != null)
                {
                    Object newValue = outputExpr.getOutputVariable().getValue();
                    columnData.setValue(groupElement, newValue);
                }
            }
            tableReader.close();
            getFormulaExpr().evaluateEnd();
        }
        else
        {
            throw new UnsupportedOperationException("This type of column definition is not implemented.");
        }

        _dim.getData().reindex();
        _dim.getData().setAutoIndex(true);
    }

    //
    // Dependencies
    //

    public List<Dim> dependencies;

    @Override
    public List<DcTable> usesTables(boolean recursive) // This element depends upon
    {
        throw new UnsupportedOperationException("TODO");
    }
    @Override
    public List<DcTable> isUsedInTables(boolean recursive) // Dependants
    {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<DcColumn> usesColumns(boolean recursive) // This element depends upon
    {
        throw new UnsupportedOperationException("TODO");
    }
    @Override
    public List<DcColumn> isUsedInColumns(boolean recursive) // Dependants
    {
        throw new UnsupportedOperationException("TODO");
    }

    public ColumnDefinition(DcColumn dim)
    {
        _dim = dim;

        _appendData = false;
        _appendSchema = true;

        dependencies = new ArrayList<Dim>();
    }

}
