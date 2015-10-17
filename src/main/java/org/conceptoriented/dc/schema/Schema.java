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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.conceptoriented.dc.data.DcTableWriter;
import org.conceptoriented.dc.schema.*;

import com.google.common.io.Files;

public class Schema extends Set implements DcSchema {

    //
    // ComSchema interface
    //

    protected DcWorkspace workspace;
    @Override
    public DcWorkspace getWorkspace() {
        return workspace;
    }
    @Override
    public void setWorkspace(DcWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public DcTable getPrimitive(String dataType) {
        Optional<DcColumn> col = getSubColumns().stream().filter(x -> Utils.sameTableName(x.getInput().getName(), dataType)).findAny();
        return col.isPresent() ? col.get().getInput() : null;
    }

    @Override
    public DcTable getRoot() {
        return getPrimitive("Root");
    }

    // Table factory

    @Override
    public DcTable createTable(String name) {
        DcTable table = new Set(name);
        return table;
    }

    @Override
    public DcTable addTable(DcTable table, DcTable parent, String superName) {
        if (parent == null)
        {
            parent = getRoot();
        }
        if (Utils.isNullOrEmpty(superName))
        {
            superName = "Super";
        }

        Dim dim = new Dim(superName, table, parent, true, true);

        dim.add();

        return table;
    }

    @Override
    public void deleteTable(DcTable table) {
        List<DcColumn> toRemove;
        toRemove = new ArrayList<DcColumn>(table.getInputColumns());
        for (DcColumn col : toRemove)
        {
            col.remove();
        }
        toRemove = new ArrayList<DcColumn>(table.getColumns());
        for (DcColumn col : toRemove)
        {
            col.remove();
        }
    }

    @Override
    public void renameTable(DcTable table, String newName) {
        tableRenamed(table, newName); // Rename with propagation
        table.setName(newName);
    }

    // Column factory

    @Override
    public DcColumn createColumn(String name, DcTable input, DcTable output, boolean isKey) {

        DcColumn dim = new Dim(name, input, output, isKey, false);

        return dim;
    }
    @Override
    public void deleteColumn(DcColumn column) {
        columnDeleted(column);
        column.remove();
    }
    @Override
    public void renameColumn(DcColumn column, String newName) {
        columnRenamed(column, newName); // Rename with propagation
        column.setName(newName);
    }

    protected void columnRenamed(DcColumn column, String newName) {
        throw new UnsupportedOperationException();
    }

    protected void tableRenamed(DcTable table, String newName) {
        throw new UnsupportedOperationException();
    }

    protected void columnDeleted(DcColumn column) {
        throw new UnsupportedOperationException();
    }

    public DcTable createFromCsv(String fileName, boolean hasHeaderRecord) {

        //
        // Read schema and sample values which are used to suggest mappings
        //
        String tableName = "New Table";
        List<String> sourceNames = new ArrayList<String>();
        List<List<String>> sampleValues = new ArrayList<List<String>>();
        List<String> targetTypes = new ArrayList<String>();
        List<DcColumn> columns = new ArrayList<DcColumn>();

        try {
            File file = new File(fileName);
            tableName = file.getName();
            tableName = Files.getNameWithoutExtension(tableName);

            Reader in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

            // Read several lines and collect sample values for suggesting types
            int recordNumber = 0;
            for (CSVRecord record : records) {
                if(recordNumber == 0) { // Get column names from this record
                    for(int i=0; i<record.size(); i++) {
                        sampleValues.add(new ArrayList<String>());

                        if(hasHeaderRecord) {
                            sourceNames.add(record.get(i));
                        }
                        else {
                            sourceNames.add("Column " + (i+1));
                            sampleValues.get(i).add(record.get(i));
                        }
                    }
                }
                else { // Get sample values from this record
                    for(int i=0; i<record.size(); i++) {
                        sampleValues.get(i).add(record.get(i));
                    }
                }

                recordNumber++;
                if(recordNumber > 10) break;
            }

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //
        // Recommend target types
        //
        for(int i=0; i<sourceNames.size(); i++) {
            String targetTypeName;
            if (Utils.isInt32(sampleValues.get(i).toArray(new String[0]))) targetTypeName = "Integer";
            else if (Utils.isDouble(sampleValues.get(i).toArray(new String[0]))) targetTypeName = "Double";
            else targetTypeName = "String";

            targetTypes.add(targetTypeName);
        }

        //
        // Create table with columns according to the mappings and target types
        //
        DcTable table = this.createTable(tableName);

        for(int i=0; i<sourceNames.size(); i++) {
            DcColumn column = createColumn(sourceNames.get(i), table, this.getPrimitive(targetTypes.get(i)), false);
            column.add();
            column.getData().setAutoIndex(false); // We will do many appends
            columns.add(column);
        }

        //
        // Read data according to the schema
        //
        DcColumn[] columnArray = columns.toArray(new DcColumn[0]);
        Object[] valueArray = new Object[columnArray.length];
        try {
        	DcTableWriter tableWriter = table.getTableWriter();
        	tableWriter.open();
        	
            Reader in = new FileReader(fileName);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

            // Read several lines and collect sample values for suggesting types
            int recordNumber = 0;
            for (CSVRecord record : records) {
                if(recordNumber == 0 && hasHeaderRecord) {
                    recordNumber++;
                    continue;
                }

                for(int i=0; i<record.size(); i++) {
                    String value = record.get(i);
                    valueArray[i] = value;
                }

                tableWriter.append(columnArray, valueArray);

                recordNumber++;
            }

            in.close();
        	tableWriter.close();
        	
        } catch (IOException e) {
            e.printStackTrace();
        }

        table.getData().reindex();
        table.getData().setAutoIndex(true);

        return table;
    }

    protected void createDataTypes() // Create all primitive data types from some specification like Enum, List or XML
    {
        Set set;
        Dim dim;

        set = new Set("Root");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Integer");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Double");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Decimal");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("String");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("Boolean");
        dim = new Dim("Top", set, this, true, true);
        dim.add();

        set = new Set("DateTime");
        dim = new Dim("Top", set, this, true, true);
        dim.add();
    }

    public Schema() {
        this("");
    }

    public Schema(String name) {
        super(name);

        createDataTypes(); // Generate all predefined primitive sets as subsets
    }

}
