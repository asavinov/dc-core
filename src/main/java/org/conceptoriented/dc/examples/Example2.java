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

public class Example2 {

    public static void main(String[] args) {
        String path = "src/test/resources/example3"; // Relative to project directory

        String detailsTableName = "Sales_SalesOrderDetail.txt";
        String productsTableName = "Production_Product.txt";
        String subCategoriesTableName = "Production_ProductSubcategory.txt";
        String categoriesTableName = "Production_ProductCategory.txt";

        Space space = new Space();
        DcSchema schema = space.createSchema("Example 2", DcSchemaKind.Dc);

        DcTable integerType = schema.getPrimitiveType("Integer");
        DcTable doubleType = schema.getPrimitiveType("Double");

        //
        // Load data from CSV files
        //
        long loadStartTime = System.currentTimeMillis();

        DcTable detailsTable = ((Schema)schema).createFromCsv(path + "\\" + detailsTableName, true);
        DcTable productsTable = ((Schema)schema).createFromCsv(path + "\\" + productsTableName, true);
        DcTable subCategoriesTable = ((Schema)schema).createFromCsv(path + "\\" + subCategoriesTableName, true);
        DcTable categoriesTable = ((Schema)schema).createFromCsv(path + "\\" + categoriesTableName, true);

        //
        // Create, define and evaluate columns
        //
        long evaluationStartTime = System.currentTimeMillis();

        // Arithmetic columns: output is a computed primitive value
        DcColumn amountColumn = space.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getData().setFormula("[UnitPrice] * [OrderQty]");
        amountColumn.getData().evaluate();

        //
        // Link columns: output is a tuple
        //
        DcColumn productColumn = space.createColumn("Product", detailsTable, productsTable, false);
        productColumn.getData().setFormula("(( Integer [ProductID] = [ProductID] ))");
        productColumn.getData().evaluate();

        DcColumn subCategoryColumn = space.createColumn("SubCategory", productsTable, subCategoriesTable, false);
        subCategoryColumn.getData().setFormula("(( Integer [ProductSubcategoryID] = [ProductSubcategoryID] ))");
        subCategoryColumn.getData().evaluate();

        DcColumn categoryColumn = space.createColumn("Category", subCategoriesTable, categoriesTable, false);
        categoryColumn.getData().setFormula("(( Integer [ProductCategoryID] = [ProductCategoryID] ))");
        categoryColumn.getData().evaluate();

        //
        // Aggregation columns: output is an aggregation of several values
        //
        DcColumn totalAmountColumn = space.createColumn("Total Amount", subCategoriesTable, doubleType, false);
        totalAmountColumn.getData().setFormula("AGGREGATE(facts=[Sales_SalesOrderDetail], groups=[Product].[SubCategory], measure=[Amount], aggregator=SUM)");
        totalAmountColumn.getData().evaluate();

        DcColumn totalCategoryColumn = space.createColumn("Total Amount Category", categoriesTable, doubleType, false);
        totalCategoryColumn.getData().setFormula("AGGREGATE(facts=[Sales_SalesOrderDetail], groups=[Product].[SubCategory].[Category], measure=[Amount], aggregator=SUM)");
        totalCategoryColumn.getData().evaluate();

        DcColumn totalCategoryColumn2 = space.createColumn("Total Amount Category 2", categoriesTable, doubleType, false);
        totalCategoryColumn2.getData().setFormula("AGGREGATE(facts=[Production_ProductSubcategory], groups=[Category], measure=[Total Amount], aggregator=SUM)");
        totalCategoryColumn2.getData().evaluate();

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

        for(int i=0; i<subCategoriesTable.getData().getLength(); i++) {
            System.out.format("%-5s%-20s%16.5f%n",
                    i,
                    subCategoriesTable.getColumn("Name").getData().getValue(i),
                    totalAmountColumn.getData().getValue(i)
                    );
//          0:36607842.80999795
//          34:null
//          36:246452.21999997256
        }

        System.out.println();

        for(int i=0; i<categoriesTable.getData().getLength(); i++) {
            System.out.format("%-5s%-20s%16.5f%16.5f%n",
                    i,
                    categoriesTable.getColumn("Name").getData().getValue(i),
                    totalCategoryColumn.getData().getValue(i),
                    totalCategoryColumn2.getData().getValue(i)
                    );
//          95124816.93995452
//          11810498.099999791
//          2141353.770000085
//          1278729.2399997374
        }

    }

}
