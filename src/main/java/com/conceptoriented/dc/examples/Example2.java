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

package com.conceptoriented.dc.examples;

import com.conceptoriented.dc.*;

public class Example2 {

    public static void main(String[] args) {
        String path = "src/test/resources/example3"; // Relative to project directory

        String detailsTableName = "Sales_SalesOrderDetail.txt";
        String productsTableName = "Production_Product.txt";
        String subCategoriesTableName = "Production_ProductSubcategory.txt";
        String categoriesTableName = "Production_ProductCategory.txt";

        Workspace workspace = new Workspace();
        DcSchema schema = new Schema("Example 1");
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        DcTable integerType = schema.getPrimitive("Integer");
        DcTable doubleType = schema.getPrimitive("Double");

        //
        // Load data from CSV files
        //
        long loadStartTime = System.currentTimeMillis();

        DcTable detailsTable = ((Schema)schema).createFromCsv(path + "\\" + detailsTableName, true);
        schema.addTable(detailsTable, null, null);
        DcTable productsTable = ((Schema)schema).createFromCsv(path + "\\" + productsTableName, true);
        schema.addTable(productsTable, null, null);
        DcTable subCategoriesTable = ((Schema)schema).createFromCsv(path + "\\" + subCategoriesTableName, true);
        schema.addTable(subCategoriesTable, null, null);
        DcTable categoriesTable = ((Schema)schema).createFromCsv(path + "\\" + categoriesTableName, true);
        schema.addTable(categoriesTable, null, null);

        //
        // Create, define and evaluate columns
        //
        long evaluationStartTime = System.currentTimeMillis();

        // Arithmetic columns: output is a computed primitive value
        DcColumn amountColumn = schema.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getDefinition().setFormula("[UnitPrice] * [OrderQty]");
        amountColumn.add();
        amountColumn.getDefinition().evaluate();

        //
        // Link columns: output is a tuple
        //
        DcColumn productColumn = schema.createColumn("Product", detailsTable, productsTable, false);
        productColumn.getDefinition().setFormula("(( Integer [ProductID] = [ProductID] ))");
        productColumn.add();
        productColumn.getDefinition().evaluate();

        DcColumn subCategoryColumn = schema.createColumn("SubCategory", productsTable, subCategoriesTable, false);
        subCategoryColumn.getDefinition().setFormula("(( Integer [ProductSubcategoryID] = [ProductSubcategoryID] ))");
        subCategoryColumn.add();
        subCategoryColumn.getDefinition().evaluate();

        DcColumn categoryColumn = schema.createColumn("Category", subCategoriesTable, categoriesTable, false);
        categoryColumn.getDefinition().setFormula("(( Integer [ProductCategoryID] = [ProductCategoryID] ))");
        categoryColumn.add();
        categoryColumn.getDefinition().evaluate();

        //
        // Aggregation columns: output is an aggregation of several values
        //
        DcColumn totalAmountColumn = schema.createColumn("Total Amount", subCategoriesTable, doubleType, false);
        totalAmountColumn.getDefinition().setFormula("AGGREGATE(facts=[Sales_SalesOrderDetail], groups=[Product].[SubCategory], measure=[Amount], aggregator=SUM)");
        totalAmountColumn.add();
        totalAmountColumn.getDefinition().evaluate();

        DcColumn totalCategoryColumn = schema.createColumn("Total Amount Category", categoriesTable, doubleType, false);
        totalCategoryColumn.getDefinition().setFormula("AGGREGATE(facts=[Sales_SalesOrderDetail], groups=[Product].[SubCategory].[Category], measure=[Amount], aggregator=SUM)");
        totalCategoryColumn.add();
        totalCategoryColumn.getDefinition().evaluate();

        DcColumn totalCategoryColumn2 = schema.createColumn("Total Amount Category 2", categoriesTable, doubleType, false);
        totalCategoryColumn2.getDefinition().setFormula("AGGREGATE(facts=[Production_ProductSubcategory], groups=[Category], measure=[Total Amount], aggregator=SUM)");
        totalCategoryColumn2.add();
        totalCategoryColumn2.getDefinition().evaluate();

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
