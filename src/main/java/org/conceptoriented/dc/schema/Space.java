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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.conceptoriented.dc.schema.*;

public class Space implements DcSpace {

    //
    // DcSchemas
    //

    protected List<DcSchema> _schemas;

    @Override
    public DcSchema createSchema(String name, DcSchemaKind schemaType)
    {
        DcSchema schema;

        if (schemaType == DcSchemaKind.Dc)
        {
            schema = new Schema(name, this);
        }
        else if (schemaType == DcSchemaKind.Csv)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else if (schemaType == DcSchemaKind.Oledb)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else if (schemaType == DcSchemaKind.Rel)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }

        _schemas.add(schema);

        return schema;
    }

    @Override
    public void deleteSchema(DcSchema schema)
    {
        // We have to ensure that inter-schema (import/export) columns are also deleted
        List<DcTable> allTables = this.getTables(schema); // schema.AllSubTables;
        for (DcTable t : allTables)
        {
            if (t.isPrimitive()) continue;
            this.deleteTable(t);
        }

        _schemas.remove(schema);
    }
    @Override
    public List<DcSchema> getSchemas()
    {
        return new ArrayList<DcSchema>(_schemas);
    }
    @Override
    public DcSchema getSchema(String name)
    {
        Optional<DcSchema> ret = _schemas.stream().filter(x -> Utils.sameTableName(x.getName(), name)).findAny();
        return ret.isPresent() ? ret.get() : null;
    }

    //
    // Tables
    //

    protected List<DcTable> _tables;

    @Override
    public DcTable createTable(String name, DcTable parent)
    {
        DcSchema schema = parent.getSchema();
        DcSchemaKind schemaType = schema.getSchemaKind();

        DcTable table;
        Column column;
        String colName;
        if (parent instanceof DcSchema)
        {
            colName = "Top";
        }
        else
        {
            colName = "Super";
        }

        if (schemaType == DcSchemaKind.Dc)
        {
            table = new Table(name, this);
            column = new Column(colName, table, parent, true, true);
        }
        else if (schemaType == DcSchemaKind.Csv)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else if (schemaType == DcSchemaKind.Oledb)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else if (schemaType == DcSchemaKind.Rel)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }

        _tables.add(table);
        _columns.add(column);

        return table;
    }

    @Override
    public void deleteTable(DcTable table)
    {
        List<DcColumn> toRemove;
        toRemove = new ArrayList<DcColumn>(table.getInputColumns());
        for (DcColumn col : toRemove)
        {
            this.deleteColumn(col);
        }
        toRemove = new ArrayList<DcColumn>(table.getColumns());
        for (DcColumn col : toRemove)
        {
            this.deleteColumn(col);
        }

        _tables.remove(table);
    }

    @Override
    public List<DcTable> getTables(DcSchema schema)
    {
        if(schema == null)
        {
            return new ArrayList<DcTable>(_tables);
        }
        else
        {
            return _tables.stream().filter(x -> x.getSchema() == schema).collect(Collectors.toList());
        }
    }

    //
    // Columns
    //

    protected List<DcColumn> _columns;

    @Override
    public DcColumn createColumn(String name, DcTable input, DcTable output, boolean isKey)
    {
        DcSchema inSchema = input.getSchema();
        DcSchemaKind inSchemaType = inSchema.getSchemaKind();

        DcColumn column;

        if (inSchemaType == DcSchemaKind.Dc)
        {
            column = new Column(name, input, output, isKey, false);
        }
        else if (inSchemaType == DcSchemaKind.Csv)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else if (inSchemaType == DcSchemaKind.Oledb)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else if (inSchemaType == DcSchemaKind.Rel)
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }
        else
        {
            throw new UnsupportedOperationException("This schema type is not implemented.");
        }

        _columns.add(column);

        return column;
    }
    @Override
    public void deleteColumn(DcColumn column)
    {
        _columns.remove(column);
    }
    @Override
    public List<DcColumn> getColumns(DcTable table)
    {
        if (table == null)
        {
            return new ArrayList<DcColumn>(_columns);
        }
        else
        {
            return _columns.stream().filter(x -> x.getInput() == table).collect(Collectors.toList());
        }
    }
    @Override
    public List<DcColumn> getInputColumns(DcTable table)
    {
        if (table == null)
        {
            return new ArrayList<DcColumn>(_columns);
        }
        else
        {
            return _columns.stream().filter(x -> x.getOutput() == table).collect(Collectors.toList());
        }
    }
    
    public Space() {
        _schemas = new ArrayList<DcSchema>();
        _tables = new ArrayList<DcTable>();
        _columns = new ArrayList<DcColumn>();
    }
}
