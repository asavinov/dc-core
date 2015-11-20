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

public class ColumnDataEmpty implements DcColumnData
{
    protected int _length;
    @Override
    public int getLength() {
        return _length;
    }
    @Override
    public void setLength(int value) {
        _length = value;
    }

    protected boolean _autoIndex;
    @Override
    public boolean isAutoIndex() {
        return _autoIndex;
    }
    @Override
    public void setAutoIndex(boolean value) {
        _autoIndex = value;
    }

    protected boolean _indexed;
    @Override
    public boolean isIndexed() {
        return _indexed;
    }

    @Override
    public void reindex() { }

    @Override
    public boolean isNull(int input) { return true; }

    @Override
    public Object getValue(int input) { return null; }

    @Override
    public void setValue(int input, Object value) { }
    @Override
    public void setValue(Object value) { }

    @Override
    public void nullify() { }

    @Override
    public void append(Object value) { }

    @Override
    public void insert(int input, Object value) { }

    @Override
    public void remove(int input) { }

    @Override
    public Object project(int[] offsets) { return null; }

    @Override
    public int[] deproject(Object value) { return null; } // Or empty array

    //
    // The former DcColumnDefinition 
    //

    @Override
    public String getFormula() { return null; }
    @Override
    public void setFormula(String formula) {}

    //
    // Structured (object) representation
    //

    @Override
    public boolean isAppendData() { return false; }
    @Override
    public void setAppendData(boolean value) {}

    @Override
    public boolean isAppendSchema() { return false; }
    @Override
    public void setAppendSchema(boolean value) {}

    @Override
    public ExprNode getFormulaExpr() { return null; }
    @Override
    public void setFormulaExpr(ExprNode exprNode) {}

    //
    // Compute
    //

    @Override
    public void evaluate() {}

    //
    // Dependencies. The order is important and corresponds to dependency chain
    //

    @Override
    public List<DcTable> usesTables(boolean recursive) { return null; } // This element depends upon
    @Override
    public List<DcTable> isUsedInTables(boolean recursive) { return null; } // Dependants

    @Override
    public List<DcColumn> usesColumns(boolean recursive) { return null; } // This element depends upon
    @Override
    public List<DcColumn> isUsedInColumns(boolean recursive) { return null; } // Dependants
}
