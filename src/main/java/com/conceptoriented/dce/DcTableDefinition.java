package com.conceptoriented.dce;

import java.util.List;

/**
 * Defines data in a table.
 *
 * @author savinov
 *
 */
public interface DcTableDefinition
{
    TableDefinitionType getDefinitionType();
    void setDefinitionType(TableDefinitionType value);

    ExprNode getWhereExpr();
    void setWhereExpr(ExprNode value);

    ExprNode getOrderbyExp();
    void setOrderbyExp(ExprNode value);

    DcIterator getWhereEvaluator();

    void populate();

    void unpopulate(); // Is not it Length=0?

    //
    // Dependencies. The order is important and corresponds to dependency chain
    //
    List<DcTable> usesTables(boolean recursive); // This element depends upon
    List<DcTable> isUsedInTables(boolean recursive); // Dependants

    List<DcColumn> usesColumns(boolean recursive); // This element depends upon
    List<DcColumn> isUsedInColumns(boolean recursive); // Dependants
}
