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

public class Schema extends Table implements DcSchema {

    //
    // DcSchema interface
    //

    protected DcSchemaKind _schemaKind;
    @Override
    public DcSchemaKind getSchemaKind() { return _schemaKind; }

    @Override
    public DcTable getPrimitive(String dataType) {
        Optional<DcColumn> col = getSubColumns().stream().filter(x -> Utils.sameTableName(x.getInput().getName(), dataType)).findAny();
        return col.isPresent() ? col.get().getInput() : null;
    }

    @Override
    public DcTable getRoot() {
        return getPrimitive("Root");
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
        DcTable table = getSpace().createTable(tableName, this.getRoot());

        for(int i=0; i<sourceNames.size(); i++) {
            DcColumn column = getSpace().createColumn(sourceNames.get(i), table, this.getPrimitive(targetTypes.get(i)), false);
            column.getData().setAutoIndex(false); // We will do many appends
            columns.add(column);
        }

        //
        // Read data according to the schema
        //
        DcColumn[] columnArray = columns.toArray(new DcColumn[0]);
        Object[] valueArray = new Object[columnArray.length];
        try {
        	DcTableWriter tableWriter = table.getData().getTableWriter();
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
        getSpace().createTable("Root", this);
        getSpace().createTable("Integer", this);
        getSpace().createTable("Double", this);
        getSpace().createTable("Decimal", this);
        getSpace().createTable("String", this);
        getSpace().createTable("Boolean", this);
        getSpace().createTable("DateTime", this);
    }

    public Schema(DcSpace space) {
        this("", space);
    }

    public Schema(String name, DcSpace space) {
        super(name, space);

        _schemaKind = DcSchemaKind.Dc;

        createDataTypes(); // Generate all predefined primitive sets as subsets
    }

}
