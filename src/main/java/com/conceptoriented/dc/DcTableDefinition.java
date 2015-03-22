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

package com.conceptoriented.dc;

import java.util.List;

/**
 * Defines data in a table.
 *
 * @author savinov
 *
 */
public interface DcTableDefinition
{
    DcTableDefinitionType getDefinitionType();
    void setDefinitionType(DcTableDefinitionType value);

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
