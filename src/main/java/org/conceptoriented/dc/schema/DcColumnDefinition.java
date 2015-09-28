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

package org.conceptoriented.dc.schema;

import java.util.List;

import org.conceptoriented.dc.data.eval.ExprNode;
import org.conceptoriented.dc.utils.DimPath;
import org.conceptoriented.dc.utils.Mapping;

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

    public DcColumnDefinitionType getDefinitionType();
    public void setDefinitionType(DcColumnDefinitionType columnDefinitionType);

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
