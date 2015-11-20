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

public interface DcSpace {
    //
    // Schemas
    //
    public DcSchema createSchema(String name, DcSchemaKind schemaType);
    public void deleteSchema(DcSchema schema);
    public List<DcSchema> getSchemas();
    public DcSchema getSchema(String name);
    
    //
    // Tables
    //
    public DcTable createTable(String name, DcTable parent);
    public void deleteTable(DcTable table);
    public List<DcTable> getTables(DcSchema schema);

    //
    // Columns
    //
    public DcColumn createColumn(String name, DcTable input, DcTable output, boolean isKey);
    public void deleteColumn(DcColumn column);
    public List<DcColumn> getColumns(DcTable table);
    public List<DcColumn> getInputColumns(DcTable table);
}
