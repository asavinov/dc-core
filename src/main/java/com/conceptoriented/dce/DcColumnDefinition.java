package com.conceptoriented.dce;

import java.util.List;

/**
 * Describes and computes one function in terms of other functions.
 *
 * @author savinov
 *
 */
public interface DcColumnDefinition {

    public boolean isAppendData();
    public void setAppendData(boolean value);

    public boolean isAppendSchema();
    public void setAppendSchema(boolean value);

    public ColumnDefinitionType getDefinitionType();
    public void setDefinitionType(ColumnDefinitionType columnDefinitionType);

    //
    // COEL (language) representation
    //

    public String getFormula();
    public void setFormula(String formula);

    //
    // Structured (object) representation
    //

    public ExprNode getFormulaExpr();
    public void setFormulaExpr(ExprNode exprNode);

    public Mapping getMapping();
    public void setMapping(Mapping mapping);

    public ExprNode getWhereExpr();
    public void setWhereExpr(ExprNode exprNode);

    //
    // Aggregation
    //

    public DcTable getFactTable();
    public void setFactTable(DcTable table);

    public List<DimPath> getGroupPaths();
    public void setGroupPaths(List<DimPath> groupPaths);

    public List<DimPath> getMeasurePaths();
    public void setMeasurePaths(List<DimPath> measurePaths);

    public String getUpdater();
    public void setUpdater(String updater);


    //
    // Compute
    //

    public void evaluate();

    //
    // Dependencies. The order is important and corresponds to dependency chain
    //

    List<DcTable> usesTables(boolean recursive); // This element depends upon
    List<DcTable> isUsedInTables(boolean recursive); // Dependants

    List<DcColumn> usesColumns(boolean recursive); // This element depends upon
    List<DcColumn> isUsedInColumns(boolean recursive); // Dependants

}
