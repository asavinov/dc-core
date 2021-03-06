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

import java.util.Optional;

import org.conceptoriented.dc.schema.*;

import com.google.common.base.Strings;

public class Variable implements DcVariable
{
    protected boolean _isNull;
    Object _value;


    //
    // ComVariable interface
    //

    protected String _name;
    @Override
    public String getName() { return _name; }
    @Override
    public void getName(String name) { _name = name; }


    protected String _schemaName;
    @Override
    public String getSchemaName() { return _schemaName; }
    @Override
    public void setSchemaName(String value) { _schemaName = value; }

    protected String _typeName;
    @Override
    public String getTypeName() { return _typeName; }
    @Override
    public void setTypeName(String value) { _typeName = value; }


    public void resolve(DcSpace space) {
        if (!Strings.isNullOrEmpty(getSchemaName()))
        {
            // 1. Resolve schema name
            setTypeSchema(space.getSchema(getSchemaName()));

            if (getTypeSchema() == null) return; // Cannot resolve

            // 2. Resolve table name
            setTypeTable(getTypeSchema().getSubTable(getTypeName()));

            if (getTypeTable() == null) return; // Cannot resolve
        }
        else if (!Strings.isNullOrEmpty(getTypeName())) // No schema name (imcomplete info)
        {
            // 1. try to find the table in the mashup
        	Optional<DcSchema> ret = space.getSchemas().stream().filter(x -> x.getSchemaKind() == DcSchemaKind.Dc).findAny();
            DcSchema mashup = ret.isPresent() ? ret.get() : null;
            if (mashup != null)
            {
                setTypeTable(mashup.getSubTable(getTypeName()));
                if (getTypeTable() != null)
                {
                    setTypeSchema(mashup);
                    setSchemaName(getTypeSchema().getName()); // We also reconstruct the name
                    return;
                }
            }

            // 2. try to find the table in any other schema
            for (DcSchema schema : space.getSchemas())
            {
                setTypeTable(schema.getSubTable(getTypeName()));
                if (getTypeTable() != null)
                {
                    setTypeSchema(schema);
                    setSchemaName(getTypeSchema().getName()); // We also reconstruct the name
                    return;
                }
            }
        }

    }

    protected DcSchema _typeSchema;
    @Override
    public DcSchema getTypeSchema() { return _typeSchema; }
    @Override
    public void setTypeSchema(DcSchema value) { _typeSchema = value; }

    protected DcTable _typeTable;
    @Override
    public DcTable getTypeTable() { return _typeTable; }
    @Override
    public void setTypeTable(DcTable value) { _typeTable = value; }

    @Override
    public boolean isNull() { return _isNull; }

    @Override
    public Object getValue() {
        return _isNull ? null : _value;
    }

    @Override
    public void setValue(Object value) {
        if (value == null)
        {
            _value = null;
            _isNull = true;
        }
        else
        {
            _value = value;
            _isNull = false;
        }
    }

    @Override
    public void nullify()
    {
        _isNull = true;
    }

    public Variable(String schema, String type, String name)
    {
        _schemaName = schema;
        _typeName = type;

        _name = name;

        _isNull = true;
        _value = null;
    }

    public Variable(DcTable type, String name)
    {
        _schemaName = type.getSchema().getName();
        _typeName = type.getName();

        _typeSchema = type.getSchema();
        _typeTable = type;

        _name = name;

        _isNull = true;
        _value = null;
    }
}
