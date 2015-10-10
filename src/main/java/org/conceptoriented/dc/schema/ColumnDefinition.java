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

    private void evaluateBegin()
    {
        _dim.getData().setAutoIndex(false);
        //_dim.getData().nullify();
    }

    @Override
    public void evaluate()
    {
    	//
        // Get an object which is used to compute the function values according to the formula
    	//
        DcEvaluator evaluator = null;
        if (getFormulaExpr() == null || getFormulaExpr().getDefinitionType() == ColumnDefinitionType.FREE)
        {
            ; // Nothing to do
        }
        else if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.AGGREGATION)
        {
            evaluator = new EvaluatorAggr(_dim);
        }
        else if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.ARITHMETIC)
        {
            evaluator = new EvaluatorExpr(_dim);
        }
        else if (getFormulaExpr().getDefinitionType() == ColumnDefinitionType.LINK)
        {
            evaluator = new EvaluatorExpr(_dim);
        }
        else
        {
            throw new UnsupportedOperationException("This type of column definition is not implemented.");
        }
        if (evaluator == null) return;

        //
        // Evaluation loop: read next input, pass it to the expression and evaluate
        //
        
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
        _appendSchema = true;

        dependencies = new ArrayList<Dim>();
    }

}
