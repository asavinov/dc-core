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

package org.conceptoriented.dc.api;

import java.util.List;

/**
 * Describes one table.
 *
 * @author savinov
 *
 */
public interface DcTable {

    public String getName();
    public void setName(String name);

    public boolean isPrimitive();

    //
    // Outputs
    //
    public List<DcColumn> getColumns();

    public DcColumn getSuperColumn();
    public DcTable getSuperTable();
    public DcSchema getSchema();

    //
    // Inputs
    //
    public List<DcColumn> getInputColumns();

    public List<DcColumn> getSubColumns();
    public List<DcTable> getSubTables();
    public List<DcTable> getAllSubTables();

    //
    // Poset relation
    //

    boolean isSubTable(DcTable parent); // Is subset of the specified table
    boolean isInput(DcTable set); // Is lesser than the specified table
    boolean isLeast(); // Has no inputs
    boolean isGreatest(); // Has no outputs

    //
    // Names
    //
    DcColumn getColumn(String name); // Greater column
    DcTable getTable(String name); // TODO: Greater table/type - not subtable
    DcTable getSubTable(String name); // Subtable

    public DcTableData getData();
    public DcTableDefinition getDefinition();
}

