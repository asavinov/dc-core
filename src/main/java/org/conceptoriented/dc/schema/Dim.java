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

import java.math.BigDecimal;
import java.time.Instant;

import org.conceptoriented.dc.schema.*;
import org.conceptoriented.dc.data.*;

public class Dim implements DcColumn {

    //
    // ComColumn interface
    //

    protected String _name;
    @Override
    public String getName() {
        return _name;
    }
    @Override
    public void setName(String value) {
        this._name = value;
    }

    protected boolean _key;
    @Override
    public boolean isKey() {
        return _key;
    }
    @Override
    public void setKey(boolean value) {
        _key = value;
    }

    protected boolean _super;
    @Override
    public boolean isSuper() {
        return _super;
    }
    public void setSuper(boolean value) {
        _super = value;
    }

    @Override
    public boolean isPrimitive() {
        return _output == null ? false : _output.isPrimitive();
    }

    protected DcTable _input;
    @Override
    public DcTable getInput() {
        return _input;
    }
    @Override
    public void setInput(DcTable value) {
        if (_input == value) return;
        _input = value;
    }

    protected DcTable _output;
    @Override
    public DcTable getOutput() {
        return _output;
    }
    @Override
    public void setOutput(DcTable value) {
        if (_output == value) return;
        _output = value;
        _data = CreateColumnData(_output, this);
    }

    @Override
    public void add() {
        assert _input != null && _output != null;

        if (_super) // Only one super-dim per table can exist
        {
            if (_input != null && _input.getSuperColumn() != null)
            {
                _input.getSuperColumn().remove(); // Replace the existing column by the new one
            }
        }

        if (_output != null) _output.getInputColumns().add(this);
        if (_input != null) _input.getColumns().add(this);
    }

    @Override
    public void remove() {
        assert _input != null && _output != null;

        if (_output != null) _output.getInputColumns().remove(this);
        if (_input != null) _input.getColumns().remove(this);
    }

    protected DcColumnData _data;
    @Override
    public DcColumnData getData() {
        return _data;
    }

    protected DcColumnDefinition _definition;
    @Override
    public DcColumnDefinition getDefinition() {
        return _definition;
    }

    public static DcColumnData CreateColumnData(DcTable type, DcColumn column)
    {
        DcColumnData colData = new DimDataEmpty();


        if (type == null || Utils.isNullOrEmpty(type.getName()))
        {
        }
        else if (Utils.sameTableName(type.getName(), "Void"))
        {
        }
        else if (Utils.sameTableName(type.getName(), "Top"))
        {
        }
        else if (Utils.sameTableName(type.getName(), "Bottom")) // Not possible by definition
        {
        }
        else if (Utils.sameTableName(type.getName(), "Root"))
        {
        }
        else if (Utils.sameTableName(type.getName(), "Integer"))
        {
            colData = new DimData<Integer>(column);
        }
        else if (Utils.sameTableName(type.getName(), "Double"))
        {
            colData = new DimData<Double>(column);
        }
        else if (Utils.sameTableName(type.getName(), "Decimal"))
        {
            colData = new DimData<BigDecimal>(column);
        }
        else if (Utils.sameTableName(type.getName(), "String"))
        {
            colData = new DimData<String>(column);
        }
        else if (Utils.sameTableName(type.getName(), "Boolean"))
        {
            colData = new DimData<Boolean>(column);
        }
        else if (Utils.sameTableName(type.getName(), "DateTime"))
        {
            colData = new DimData<Instant>(column);
        }
        else if (Utils.sameTableName(type.getName(), "Set"))
        {
        }
        else // User (non-primitive) set
        {
            colData = new DimData<Integer>(column);
        }

        /*
        ComDataType dataType = output.getDataType();
        if(dataType == ComDataType.Void) {
        }
        else if(dataType == ComDataType.Top) {
        }
        else if(dataType == ComDataType.Bottom) {
        }
        else if(dataType == ComDataType.Root) {
            _data = new DimData<Integer>(this, dataType);
        }
        else if(dataType == ComDataType.Integer) {
            _data = new DimData<Integer>(this, dataType);
        }
        else if(dataType == ComDataType.Double) {
            _data = new DimData<Double>(this, dataType);
        }
        else if(dataType == ComDataType.Decimal) {
            _data = new DimData<BigDecimal>(this, dataType);
        }
        else if(dataType == ComDataType.String) {
            _data = new DimData<String>(this, dataType);
        }
        else if(dataType == ComDataType.Boolean) {
            _data = new DimData<Boolean>(this, dataType);
        }
        else if(dataType == ComDataType.DateTime) {
            _data = new DimData<Instant>(this, dataType);
        }
         */

        return colData;
    }

    //
    // Constructors
    //

    public Dim(Dim dim) {
        this();

        setName(dim.getName());

        setKey(dim.isKey());

        setInput(dim.getInput());
        setOutput(dim.getOutput());

        _data = CreateColumnData(_output, this);
        _definition = new ColumnDefinition(this);
        // TODO: Copy definition
    }

    public Dim(DcTable set) { // Empty dimension
        this("", set, set);
    }

    public Dim() {
        this("");
    }

    public Dim(String name) {
        this(name, null, null);
    }

    public Dim(String name, DcTable input, DcTable output) {
        this(name, input, output, false, false);
    }

    public Dim(String name, DcTable input, DcTable output, boolean isKey, boolean isSuper) {
        this._name = name;

        this._key = isKey;
        this._super = isSuper;

        this._input = input;
        this._output = output;

        _data = CreateColumnData(output, this);
        _definition = new ColumnDefinition(this);
    }

}
