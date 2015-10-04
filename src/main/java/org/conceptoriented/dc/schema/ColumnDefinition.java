package org.conceptoriented.dc.schema;

import java.util.ArrayList;
import java.util.List;

import org.conceptoriented.dc.utils.*;
import org.conceptoriented.dc.data.eval.*;
import org.conceptoriented.dc.data.query.*;

public class ColumnDefinition implements DcColumnDefinition
{
    protected DcColumn _dim;

    //
    // ComColumnDefinition interface
    //

    protected boolean _appendData;
    public boolean isAppendData() { return _appendData; }
    public void setAppendData(boolean value) { _appendData = value; }

    protected boolean _appendSchema;
    public boolean isAppendSchema() { return _appendSchema; }
    public void setAppendSchema(boolean value) { _appendSchema = value; }

    protected DcColumnDefinitionType _definitionType;
    @Override
    public DcColumnDefinitionType getDefinitionType() { return _definitionType; }
    @Override
    public void setDefinitionType(DcColumnDefinitionType value) { _definitionType = value; }

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

        if (expr == null) return;

        setFormulaExpr(expr);

        if(expr.getOperation() == OperationType.TUPLE) {
            setDefinitionType(DcColumnDefinitionType.LINK);
        }
        else if(expr.getOperation() == OperationType.CALL && expr.getName().equalsIgnoreCase("AGGREGATE")) {
            setDefinitionType(DcColumnDefinitionType.AGGREGATION);
        }
        else {
            setDefinitionType(DcColumnDefinitionType.ARITHMETIC);
        }
    }

    //
    // Structured (object) representation
    //

    protected ExprNode _formulaExpr;
    @Override
    public ExprNode getFormulaExpr() { return _formulaExpr; }
    @Override
    public void setFormulaExpr(ExprNode value) { _formulaExpr = value; }

    protected Mapping _mapping;
    @Override
    public Mapping getMapping() { return _mapping; }
    @Override
    public void setMapping(Mapping value) { _mapping = value; }

    protected ExprNode _whereExpr;
    @Override
    public ExprNode getWhereExpr() { return _whereExpr; }
    @Override
    public void setWhereExpr(ExprNode value) { _whereExpr = value; }

    //
    // Aggregation
    //

    protected DcTable _factTable;
    @Override
    public DcTable getFactTable() { return _factTable; }
    @Override
    public void setFactTable(DcTable value) { _factTable = value; }

    protected List<DimPath> _groupPaths;
    @Override
    public List<DimPath> getGroupPaths() { return _groupPaths; }
    @Override
    public void setGroupPaths(List<DimPath> value) { _groupPaths = value; }

    protected List<DimPath> _measurePaths;
    @Override
    public List<DimPath> getMeasurePaths() { return _measurePaths; }
    @Override
    public void setMeasurePaths(List<DimPath> value) { _measurePaths = value; }

    protected String _updater;
    @Override
    public String getUpdater() { return _updater; }
    @Override
    public void setUpdater(String value) { _updater = value; }

    //
    // Compute
    //

    // Get an object which is used to compute the function values according to the formula
    protected DcEvaluator getIterator()
    {
        DcEvaluator evaluator = null;

        if (getDefinitionType() == DcColumnDefinitionType.FREE)
        {
            ; // Nothing to do
        }
        else if (getDefinitionType() == DcColumnDefinitionType.AGGREGATION)
        {
            evaluator = new EvaluatorAggr(_dim);
        }
        else if (getDefinitionType() == DcColumnDefinitionType.ARITHMETIC)
        {
            evaluator = new EvaluatorExpr(_dim);
        }
        else if (getDefinitionType() == DcColumnDefinitionType.LINK)
        {
            evaluator = new EvaluatorExpr(_dim);
        }
        else
        {
            throw new UnsupportedOperationException("This type of column definition is not implemented.");
        }

        return evaluator;
    }

    private void evaluateBegin()
    {
        _dim.getData().setAutoIndex(false);
        //_dim.getData().nullify();
    }

    @Override
    public void evaluate()
    {
        DcEvaluator evaluator = getIterator();
        if (evaluator == null) return;

        try {
            evaluateBegin();

            while (evaluator.nextInput())
            {
                evaluator.evaluate();
            }
        } catch (Exception e) {
            throw e;
        }
        finally {
            evaluateEnd();
        }

    }

    protected void evaluateEnd()
    {
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
        _definitionType = DcColumnDefinitionType.FREE;

        _groupPaths = new ArrayList<DimPath>();
        _measurePaths = new ArrayList<DimPath>();

        dependencies = new ArrayList<Dim>();
    }

}
