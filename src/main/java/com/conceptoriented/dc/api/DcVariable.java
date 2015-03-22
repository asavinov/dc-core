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

package com.conceptoriented.dc.api;

public interface DcVariable {

    //
    // Variable name (strictly speaking, it should belong to a different interface)
    //

    public String getName();
    public void getName(String name);

    //
    // Type info
    //
    public String getSchemaName();
    public void setSchemaName(String schemaName);

    public String getTypeName();
    public void setTypeName(String typeName);

    public void resolve(DcWorkspace workspace);

    public DcSchema getTypeSchema();
    public void setTypeSchema(DcSchema typeSchema);

    public DcTable getTypeTable();
    public void setTypeTable(DcTable typeTable);

    //
    // Variable data. Analogous to the column data interface but without input argument
    //

    public boolean isNull();

    public Object getValue();
    public void setValue(Object value);

    public void nullify();

    //
    // Typed methods
    //
}
