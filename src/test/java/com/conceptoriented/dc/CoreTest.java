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

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.conceptoriented.dc.DimPath;
import com.conceptoriented.dc.ExprBuilder;
import com.conceptoriented.dc.ExprNode;

import com.conceptoriented.dc.api.*;

public class CoreTest {

    static String detailsTableName = "target/test-classes/example2/OrderDetails.csv";
    static String productsTableName = "target/test-classes/example2/Products.csv";
    static String categoriesTableName = "target/test-classes/example2/Categories.csv";

    public static ExprBuilder exprBuilder;

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

        exprBuilder = new ExprBuilder();
    }

    DcWorkspace workspace;
    DcSchema schema;

    @Before
    public void setUp() {
        workspace = new Workspace();

        //
        // Prepare schema
        //
        schema = createSampleSchema();
        workspace.addSchema(schema);
        schema.setWorkspace(workspace);
    }

    protected DcSchema createSampleSchema()
    {
        // Prepare schema
        DcSchema schema = new Schema("My Schema");

        // Table 1
        DcTable t1 = schema.createTable("Table 1");
        schema.addTable(t1, schema.getRoot(), null);

        DcColumn c11 = schema.createColumn("Column 11", t1, schema.getPrimitive("Integer"), true);
        c11.add();
        DcColumn c12 = schema.createColumn("Column 12", t1, schema.getPrimitive("String"), true);
        c12.add();
        DcColumn c13 = schema.createColumn("Column 13", t1, schema.getPrimitive("Double"), false);
        c13.add();
        DcColumn c14 = schema.createColumn("Column 14", t1, schema.getPrimitive("Decimal"), false);
        c14.add();

        // Table 2
        DcTable t2 = schema.createTable("Table 2");
        schema.addTable(t2, schema.getRoot(), null);

        DcColumn c21 = schema.createColumn("Column 21", t2, schema.getPrimitive("String"), true);
        c21.add();
        DcColumn c22 = schema.createColumn("Column 22", t2, schema.getPrimitive("Integer"), true);
        c22.add();
        DcColumn c23 = schema.createColumn("Column 23", t2, schema.getPrimitive("Double"), false);
        c23.add();
        DcColumn c24 = schema.createColumn("Table 1", t2, t1, false);
        c24.add();

        return schema;
    }

    protected void createSampleData(DcSchema schema)
    {
        //
        // Fill sample data in "Table 1"
        //
        DcTable t1 = schema.getSubTable("Table 1");

        DcColumn c11 = t1.getColumn("Column 11");
        DcColumn c12 = t1.getColumn("Column 12");
        DcColumn c13 = t1.getColumn("Column 13");
        DcColumn c14 = t1.getColumn("Column 14");

        DcColumn[] cols = new DcColumn[] { c11, c12, c13, c14 };
        Object[] vals = new Object[4];

        vals[0] = 20;
        vals[1] = "Record 0";
        vals[2] = 20.0;
        vals[3] = BigDecimal.valueOf(20.0); // new BigDecimal(20.0);
        t1.getData().append(cols, vals);

        vals[0] = 10;
        vals[1] = "Record 1";
        vals[2] = 10.0;
        vals[3] = BigDecimal.valueOf(20.0); // new BigDecimal(20.0);
        t1.getData().append(cols, vals);

        vals[0] = 30;
        vals[1] = "Record 2";
        vals[2] = 30.0;
        vals[3] = BigDecimal.valueOf(20.0); // new BigDecimal(20.0);
        t1.getData().append(cols, vals);

        //
        // Fill sample data in "Table 2"
        //
        DcTable t2 = schema.getSubTable("Table 2");

        DcColumn c21 = t2.getColumn("Column 21");
        DcColumn c22 = t2.getColumn("Column 22");
        DcColumn c23 = t2.getColumn("Column 23");
        DcColumn c24 = t2.getColumn("Table 1");

        cols = new DcColumn[] { c21, c22, c23, c24 };
        vals = new Object[4];

        vals[0] = "Value A";
        vals[1] = 20;
        vals[2] = 40.0;
        vals[3] = 0;
        t2.getData().append(cols, vals);

        vals[0] = "Value A";
        vals[1] = 30;
        vals[2] = 40.0;
        vals[3] = 1;
        t2.getData().append(cols, vals);

        vals[0] = "Value A";
        vals[1] = 30;
        vals[2] = 50.0;
        vals[3] = 1;
        t2.getData().append(cols, vals);

        vals[0] = "Value B";
        vals[1] = 30;
        vals[2] = 50.0;
        vals[3] = 1;
        t2.getData().append(cols, vals);
    }

    @Test
    public void SchemaTest() // ComColumn. Manually add/remove tables/columns
    {
        DcTable t1 = schema.getSubTable("Table 1");
        DcTable t2 = schema.getSubTable("Table 2");

        // Finding by name and check various properties provided by the schema
        assertEquals(schema.getPrimitive("Decimal").getName(), "Decimal");

        assertEquals(t1.getName(), "Table 1");
        assertEquals(t2.getName(), "Table 2");
        assertEquals(schema.getSubTable("Table 2"), t2);

        assertEquals(t1.getColumn("Column 11").getName(), "Column 11");
        assertEquals(t2.getColumn("Column 21").getName(), "Column 21");

        assertEquals(t2.getColumn("Super").isSuper(), true);
        assertEquals(t2.getSuperColumn().getInput(), t2);
        assertEquals(t2.getSuperColumn().getOutput(), schema.getRoot());

        // Test path enumerator
        //var pathEnum = new PathEnumerator(t2, t1, DimensionType.IDENTITY_ENTITY);
        //Assert.AreEqual(1, pathEnum.Count());
    }

    @Test
    public void ArithmeticTest() // ComColumnDefinition. Defining new columns and evaluate them
    {
        createSampleData(schema);

        DcTable t1 = schema.getSubTable("Table 1");

        DcColumn c11 = t1.getColumn("Column 11");
        DcColumn c12 = t1.getColumn("Column 12");
        DcColumn c13 = t1.getColumn("Column 13");
        DcColumn c14 = t1.getColumn("Column 14");

        //
        // Define a derived column with a definition
        //
        DcColumn c15 = schema.createColumn("Column 15", t1, schema.getPrimitive("Double"), false);

        c15.getDefinition().setFormula("([Column 11]+10.0) * this.[Column 13]");

        c15.add();

        // Evaluate column
        c15.getDefinition().evaluate();

        assertEquals(600.0, c15.getData().getValue(0));
        assertEquals(200.0, c15.getData().getValue(1));
        assertEquals(1200.0, c15.getData().getValue(2));
    }

    @Test
    public void NativeFunctionTest() // Call native function in column definition
    {
        createSampleData(schema);

        DcTable t1 = schema.getSubTable("Table 1");

        DcColumn c11 = t1.getColumn("Column 11");
        DcColumn c12 = t1.getColumn("Column 12");
        DcColumn c13 = t1.getColumn("Column 13");
        DcColumn c14 = t1.getColumn("Column 14");

        //
        // Define a derived column with a definition
        //
        DcColumn c15 = schema.createColumn("Column 15", t1, schema.getPrimitive("String"), false);

        c15.getDefinition().setFormula("call:java.lang.String.substring( [Column 12], 7, 8 )");

        c15.add();

        // Evaluate column
        c15.getDefinition().evaluate();

        assertEquals("0", c15.getData().getValue(0));
        assertEquals("1", c15.getData().getValue(1));
        assertEquals("2", c15.getData().getValue(2));

        //
        // Define a derived column with a definition
        //
        DcColumn c16 = schema.createColumn("Column 15", t1, schema.getPrimitive("Double"), false);

        c16.getDefinition().setFormula("call:java.lang.Math.pow( [Column 11] / 10.0, [Column 13] / 10.0 )");

        c16.add();

        c16.getDefinition().evaluate();

        assertEquals(4.0, c16.getData().getValue(0));
        assertEquals(1.0, c16.getData().getValue(1));
        assertEquals(27.0, c16.getData().getValue(2));
    }

    @Test
    public void LinkTest()
    {
        createSampleData(schema);

        DcTable t1 = schema.getSubTable("Table 1");
        DcColumn c11 = t1.getColumn("Column 11"); // 20, 10, 30

        DcTable t2 = schema.getSubTable("Table 2");
        DcColumn c22 = t2.getColumn("Column 22"); // 20, 30, 30, 30

        //
        // Define a derived column with a definition
        //

        DcColumn link = schema.createColumn("Column Link", t2, t1, false);

        link.getDefinition().setFormula("(( [Integer] [Column 11] = this.[Column 22], [Decimal] [Column 14] = 20.0 ))");

        link.add();

        // Evaluate column
        link.getDefinition().evaluate();

        assertEquals(0, link.getData().getValue(0));
        assertEquals(2, link.getData().getValue(1));
        assertEquals(2, link.getData().getValue(2));
        assertEquals(2, link.getData().getValue(2));
    }

    @Test
    public void AggregationTest() // Defining new aggregated columns and evaluate them
    {
        createSampleData(schema);

        DcTable t1 = schema.getSubTable("Table 1");

        DcTable t2 = schema.getSubTable("Table 2");

        DcColumn c23 = t2.getColumn("Column 23");
        DcColumn c24 = t2.getColumn("Table 1");

        //
        // Define aggregated column
        //
        DcColumn c15 = schema.createColumn("Agg of Column 23", t1, schema.getPrimitive("Double"), false);
        c15.getDefinition().setDefinitionType(DcColumnDefinitionType.AGGREGATION);

        c15.getDefinition().setFactTable(t2); // Fact table
        c15.getDefinition().setGroupPaths(Arrays.asList(new DimPath(c24))); // One group path
        c15.getDefinition().setMeasurePaths(Arrays.asList(new DimPath(c23))); // One measure path
        c15.getDefinition().setUpdater("SUM"); // Aggregation function

        c15.add();

        //
        // Evaluate expression
        //
        c15.getData().setValue(0.0);
        c15.getDefinition().evaluate(); // {40, 140, 0}

        assertEquals(40.0, c15.getData().getValue(0));
        assertEquals(140.0, c15.getData().getValue(1));
        assertEquals(0.0, c15.getData().getValue(2)); // In fact, it has to be NaN or null (no values have been aggregated)

        //
        // Aggregation via a syntactic formula
        //
        DcColumn c16 = schema.createColumn("Agg2 of Column 23", t1, schema.getPrimitive("Double"), false);

        c16.getDefinition().setFormula("AGGREGATE(facts=[Table 2], groups=[Table 1], measure=[Column 23]*2.0 + 1, aggregator=SUM)");

        c16.add();

        c16.getData().setValue(0.0);
        c16.getDefinition().evaluate(); // {40, 140, 0}

        assertEquals(81.0, c16.getData().getValue(0));
        assertEquals(283.0, c16.getData().getValue(1));
        assertEquals(0.0, c16.getData().getValue(2));
    }

    @Test
    public void TableProductTest() // Define a new table and populate it
    {
        createSampleData(schema);

        DcTable t1 = schema.getSubTable("Table 1");
        DcTable t2 = schema.getSubTable("Table 2");

        //
        // Define a new product-set
        //
        DcTable t3 = schema.createTable("Table 3");
        t3.getDefinition().setDefinitionType(DcTableDefinitionType.PRODUCT);
        schema.addTable(t3, null, null);

        DcColumn c31 = schema.createColumn(t1.getName(), t3, t1, true); // {*20, 10, *30}
        c31.add();
        DcColumn c32 = schema.createColumn(t2.getName(), t3, t2, true); // {40, 40, *50, *50}
        c32.add();

        t3.getDefinition().populate();
        assertEquals(12, t3.getData().getLength());

        //
        // Add simple where expression
        //

        ExprNode ast = exprBuilder.build("([Table 1].[Column 11] > 10) && this.[Table 2].[Column 23] == 50.0");
        t3.getDefinition().setWhereExpr(ast);

        t3.getDefinition().populate();
        assertEquals(4, t3.getData().getLength());

        assertEquals(0, c31.getData().getValue(0));
        assertEquals(2, c32.getData().getValue(0));

        assertEquals(0, c31.getData().getValue(1));
        assertEquals(3, c32.getData().getValue(1));
    }

    @Test
    public void TableSubsetTest() // Define a filter to get a subset of record from one table
    {
        createSampleData(schema);

        DcTable t2 = schema.getSubTable("Table 2");

        //
        // Define a new filter-set
        //
        DcTable t3 = schema.createTable("Table 3");

        ExprNode ast = exprBuilder.build("[Column 22] > 20.0 && this.Super.[Column 23] < 50");
        t3.getDefinition().setWhereExpr(ast);
        t3.getDefinition().setDefinitionType(DcTableDefinitionType.PRODUCT);

        schema.addTable(t3, t2, null);

        t3.getDefinition().populate();
        assertEquals(1, t3.getData().getLength());
        assertEquals(1, t3.getSuperColumn().getData().getValue(0));
    }

    @Test
    public void ProjectionTest() // Defining new tables via function projection and populate them
    {
        createSampleData(schema);

        DcTable t2 = schema.getSubTable("Table 2");

        DcColumn c21 = t2.getColumn("Column 21");
        DcColumn c22 = t2.getColumn("Column 22");
        DcColumn c23 = t2.getColumn("Column 23");

        //
        // Project "Table 2" along "Column 21" and get 2 unique records in a new set "Value A" (3 references) and "Value B" (1 reference)
        //
        DcTable t3 = schema.createTable("Table 3");
        t3.getDefinition().setDefinitionType(DcTableDefinitionType.PROJECTION);
        schema.addTable(t3, null, null);

        DcColumn c31 = schema.createColumn("Column 31", t3, c21.getOutput(), true);
        c31.add();

        // Create a generating column
        DcColumn c24 = schema.createColumn(t3.getName(), t2, t3, false);

        c24.getDefinition().setFormula("(( [String] [Column 31] = this.[Column 21] ))");
        c24.getDefinition().setAppendData(true);

        c24.add();

        t3.getDefinition().populate();

        assertEquals(2, t3.getData().getLength());

        assertEquals(0, c24.getData().getValue(0));
        assertEquals(0, c24.getData().getValue(1));
        assertEquals(0, c24.getData().getValue(2));
        assertEquals(1, c24.getData().getValue(3));

        //
        // Defining a combination of "Column 21" and "Column 22" and project with 3 unique records in a new set
        //
        DcTable t4 = schema.createTable("Table 4");
        t4.getDefinition().setDefinitionType(DcTableDefinitionType.PROJECTION);
        schema.addTable(t4, null, null);

        DcColumn c41 = schema.createColumn("Column 41", t4, c21.getOutput(), true);
        c41.add();
        DcColumn c42 = schema.createColumn("Column 42", t4, c22.getOutput(), true);
        c42.add();

        // Create generating/import column
        DcColumn c25 = schema.createColumn(t4.getName(), t2, t4, false);

        c25.getDefinition().setFormula("(( [String] [Column 41] = this.[Column 21] , [Integer] [Column 42] = this.[Column 22] ))");
        c25.getDefinition().setAppendData(true);

        c25.add();

        t4.getDefinition().populate();

        assertEquals(3, t4.getData().getLength());

        assertEquals(0, c25.getData().getValue(0));
        assertEquals(1, c25.getData().getValue(1));
        assertEquals(1, c25.getData().getValue(2));
        assertEquals(2, c25.getData().getValue(3));
    }

    @Test
    public void CsvTest()
    {
        DcTable integerType = schema.getPrimitive("Integer");
        DcTable doubleType = schema.getPrimitive("Double");

        DcTable detailsTable = ((Schema)schema).createFromCsv(detailsTableName, true);
        schema.addTable(detailsTable, null, null);
        DcTable productsTable = ((Schema)schema).createFromCsv(productsTableName, true);
        schema.addTable(productsTable, null, null);
        DcTable categoriesTable = ((Schema)schema).createFromCsv(categoriesTableName, true);
        schema.addTable(categoriesTable, null, null);

        assertEquals(2155, detailsTable.getData().getLength());
        assertEquals(77, productsTable.getData().getLength());
        assertEquals(8, categoriesTable.getData().getLength());

        // Define a new arithmetic column: output is a computed primitive value
        DcColumn amountColumn = schema.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getDefinition().setFormula("[UnitPrice] * [Quantity]");
        amountColumn.add();
        amountColumn.getDefinition().evaluate();

        // Define two link column: output is a tuple
        DcColumn productColumn = schema.createColumn("Product", detailsTable, productsTable, false);
        productColumn.getDefinition().setFormula("(( Integer [ProductID] = [ProductID] ))");
        productColumn.add();
        productColumn.getDefinition().evaluate();

        DcColumn categoryColumn = schema.createColumn("Category", productsTable, categoriesTable, false);
        categoryColumn.getDefinition().setFormula("(( Integer [CategoryID] = [CategoryID] ))");
        categoryColumn.add();
        categoryColumn.getDefinition().evaluate();

        // Define a new aggregation column: output is an aggregation of a group of values
        DcColumn totalAmountColumn = schema.createColumn("Total Amount", categoriesTable, doubleType, false);
        totalAmountColumn.getDefinition().setFormula("AGGREGATE(facts=[OrderDetails], groups=[Product].[Category], measure=[Amount], aggregator=SUM)");
        totalAmountColumn.add();
        totalAmountColumn.getDefinition().evaluate();

        assertEquals(105268.6, totalAmountColumn.getData().getValue(6)); // cells = {286526.94999999995, 113694.75000000001, 177099.09999999995, 251330.5, 100726.8, 178188.80000000002, 105268.6, 141623.09000000003}

        DcColumn totalCountColumn = schema.createColumn("Total Count", categoriesTable, integerType, false);
        totalCountColumn.getDefinition().setFormula("AGGREGATE(facts=[OrderDetails], groups=[Product].[Category], measure=[Amount], aggregator=COUNT)");
        totalCountColumn.add();
        totalCountColumn.getDefinition().evaluate();

        assertEquals(136, totalCountColumn.getData().getValue(6)); // cells = {404, 216, 334, 366, 196, 173, 136, 330}
    }

}
