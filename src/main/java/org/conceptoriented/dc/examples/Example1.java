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

package org.conceptoriented.dc.examples;

import org.conceptoriented.dc.schema.*;

public class Example1 {

    public static void main(String[] args) {
        String path = "src/test/resources/example3"; // Relative to project directory

        String detailsTableName = "Sales_SalesOrderDetail.txt";

        DcSpace space = new Space();
        DcSchema schema = space.createSchema("Example 1", DcSchemaKind.Dc);

        DcTable integerType = schema.getPrimitiveType("Integer");
        DcTable doubleType = schema.getPrimitiveType("Double");

        //
        // Load data from CSV files
        //
        long loadStartTime = System.currentTimeMillis();

        DcTable detailsTable = ((Schema)schema).createFromCsv(path + "\\" + detailsTableName, true);

        //
        // Create, define and evaluate columns
        //
        long evaluationStartTime = System.currentTimeMillis();

        // Arithmetic columns: output is a computed primitive value
        DcColumn amountColumn = space.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getData().setFormula("[UnitPrice] * [OrderQty]");
        amountColumn.getData().evaluate();

        //
        // Print results
        //
        long endtime = System.currentTimeMillis();

        long loadSec = (evaluationStartTime - loadStartTime) / 1000;
        long loadMsec = (evaluationStartTime - loadStartTime) % 1000;
        System.out.println("Data load time: " + loadSec + "s " + loadMsec + "ms. ");

        long evalSec = (endtime - evaluationStartTime) / 1000;
        long evalMsec = (endtime - evaluationStartTime) % 1000;
        System.out.println("Evaluation time: " + evalSec + "s " + evalMsec + "ms. ");

        System.out.println();

        for(int i=0; i<10; i++) {
            System.out.format("%-5s %16.5f %d %16.5f %n",
                    i,
                    detailsTable.getColumn("UnitPrice").getData().getValue(i),
                    detailsTable.getColumn("OrderQty").getData().getValue(i),
                    amountColumn.getData().getValue(i)
                    );
        }
    }

}
