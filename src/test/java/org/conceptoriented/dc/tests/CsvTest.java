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

package org.conceptoriented.dc.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.conceptoriented.dc.data.*;
import org.conceptoriented.dc.data.query.ExprBuilder;
import org.conceptoriented.dc.schema.*;
import org.conceptoriented.dc.utils.ColumnPath;

public class CsvTest {

    static String detailsTableName = "target/test-classes/example2/OrderDetails.csv";
    static String productsTableName = "target/test-classes/example2/Products.csv";
    static String categoriesTableName = "target/test-classes/example2/Categories.csv";

    @BeforeClass
    public static void setUpClass() {

        try {
            File testFile = null;

            testFile = new File(ClassLoader.getSystemResource("example2/OrderDetails.csv").toURI());
            detailsTableName = testFile.getAbsolutePath();

            testFile = new File(ClassLoader.getSystemResource("example2/Products.csv").toURI());
            productsTableName = testFile.getAbsolutePath();

            testFile = new File(ClassLoader.getSystemResource("example2/Categories.csv").toURI());
            categoriesTableName = testFile.getAbsolutePath();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    DcSpace space;
    DcSchema schema;

    @Before
    public void setUp() {
        space = new Space();
        schema = space.createSchema("My Schema", DcSchemaKind.Dc);
        CoreTest.createSampleSchema(schema);
    }

    @Test
    public void CsvReadTest()
    {
        DcTable integerType = schema.getPrimitive("Integer");
        DcTable doubleType = schema.getPrimitive("Double");

        DcTable detailsTable = ((Schema)schema).createFromCsv(detailsTableName, true);
        DcTable productsTable = ((Schema)schema).createFromCsv(productsTableName, true);
        DcTable categoriesTable = ((Schema)schema).createFromCsv(categoriesTableName, true);

        assertEquals(2155, detailsTable.getData().getLength());
        assertEquals(77, productsTable.getData().getLength());
        assertEquals(8, categoriesTable.getData().getLength());

        // Define a new arithmetic column: output is a computed primitive value
        DcColumn amountColumn = space.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getData().setFormula("[UnitPrice] * [Quantity]");
        amountColumn.getData().evaluate();

        // Define two link column: output is a tuple
        DcColumn productColumn = space.createColumn("Product", detailsTable, productsTable, false);
        productColumn.getData().setFormula("(( Integer [ProductID] = [ProductID] ))");
        productColumn.getData().evaluate();

        DcColumn categoryColumn = space.createColumn("Category", productsTable, categoriesTable, false);
        categoryColumn.getData().setFormula("(( Integer [CategoryID] = [CategoryID] ))");
        categoryColumn.getData().evaluate();

        // Define a new aggregation column: output is an aggregation of a group of values
        DcColumn totalAmountColumn = space.createColumn("Total Amount", categoriesTable, doubleType, false);
        totalAmountColumn.getData().setFormula("AGGREGATE(facts=[OrderDetails], groups=[Product].[Category], measure=[Amount], aggregator=SUM)");
        totalAmountColumn.getData().evaluate();

        assertEquals(105268.6, totalAmountColumn.getData().getValue(6)); // cells = {286526.94999999995, 113694.75000000001, 177099.09999999995, 251330.5, 100726.8, 178188.80000000002, 105268.6, 141623.09000000003}

        DcColumn totalCountColumn = space.createColumn("Total Count", categoriesTable, integerType, false);
        totalCountColumn.getData().setFormula("AGGREGATE(facts=[OrderDetails], groups=[Product].[Category], measure=[Amount], aggregator=COUNT)");
        totalCountColumn.getData().evaluate();

        assertEquals(136, totalCountColumn.getData().getValue(6)); // cells = {404, 216, 334, 366, 196, 173, 136, 330}
    }

}
