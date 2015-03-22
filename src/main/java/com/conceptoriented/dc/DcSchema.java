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

public interface DcSchema extends DcTable {

    public Workspace getWorkspace();
    public void setWorkspace(Workspace workspace);

    public DcTable getPrimitive(String dataType);
    public DcTable getRoot();

    // Table factory

    public DcTable createTable(String name);
    public DcTable addTable(DcTable table, DcTable parent, String superName);
    public void deleteTable(DcTable table);
    public void renameTable(DcTable table, String newName);

    // Column factory

    public DcColumn createColumn(String name, DcTable input, DcTable output, boolean isKey);
    public void deleteColumn(DcColumn column);
    public void renameColumn(DcColumn column, String newName);
}

