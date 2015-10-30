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

package org.conceptoriented.dc.data;

import java.util.List;

import org.conceptoriented.dc.schema.DcColumn;
import org.conceptoriented.dc.schema.DcTable;

/**
 * Defines data in a table.
 *
 * @author savinov
 *
 */
public interface DcTableDefinition
{
    public TableDefinitionType getDefinitionType();

    public String getWhereFormula();
    public void setWhereFormula(String value);

    public ExprNode getWhereExpr();
    public void setWhereExpr(ExprNode value);

    public String getOrderbyFormula();
    public void setOrderbyFormula(String value);

    public void populate();

    public void unpopulate(); // Is not it Length=0?

    //
    // Dependencies. The order is important and corresponds to dependency chain
    //
    public List<DcTable> usesTables(boolean recursive); // This element depends upon
    public List<DcTable> isUsedInTables(boolean recursive); // Dependants

    public List<DcColumn> usesColumns(boolean recursive); // This element depends upon
    public List<DcColumn> isUsedInColumns(boolean recursive); // Dependants
}
