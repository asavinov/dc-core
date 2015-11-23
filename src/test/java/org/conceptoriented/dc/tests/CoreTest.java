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

public class CoreTest {

    public static ExprBuilder exprBuilder;

    @BeforeClass
    public static void setUpClass() {
        exprBuilder = new ExprBuilder();
    }

    DcSpace space;
    DcSchema schema;

    @Before
    public void setUp() {
        space = new Space();
        schema = space.createSchema("My Schema", DcSchemaKind.Dc);
        createSampleSchema(schema);
    }

    public static void createSampleSchema(DcSchema schema)
    {
        DcSpace space = schema.getSpace();

        // Table 1
        DcTable t1 = space.createTable("Table 1", schema.getRoot());

        DcColumn c11 = space.createColumn("Column 11", t1, schema.getPrimitiveType("Integer"), true);
        DcColumn c12 = space.createColumn("Column 12", t1, schema.getPrimitiveType("String"), true);
        DcColumn c13 = space.createColumn("Column 13", t1, schema.getPrimitiveType("Double"), false);
        DcColumn c14 = space.createColumn("Column 14", t1, schema.getPrimitiveType("Decimal"), false);

        // Table 2
        DcTable t2 = space.createTable("Table 2", schema.getRoot());

        DcColumn c21 = space.createColumn("Column 21", t2, schema.getPrimitiveType("String"), true);
        DcColumn c22 = space.createColumn("Column 22", t2, schema.getPrimitiveType("Integer"), true);
        DcColumn c23 = space.createColumn("Column 23", t2, schema.getPrimitiveType("Double"), false);
        DcColumn c24 = space.createColumn("Table 1", t2, t1, false);
    }

    public static void createSampleData(DcSchema schema)
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
        DcTableWriter w1 = t1.getData().getTableWriter();

        vals[0] = 20;
        vals[1] = "Record 0";
        vals[2] = 20.0;
        vals[3] = BigDecimal.valueOf(20.0); // new BigDecimal(20.0);
        w1.append(cols, vals);

        vals[0] = 10;
        vals[1] = "Record 1";
        vals[2] = 10.0;
        vals[3] = BigDecimal.valueOf(20.0); // new BigDecimal(20.0);
        w1.append(cols, vals);

        vals[0] = 30;
        vals[1] = "Record 2";
        vals[2] = 30.0;
        vals[3] = BigDecimal.valueOf(20.0); // new BigDecimal(20.0);
        w1.append(cols, vals);

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
        DcTableWriter w2 = t2.getData().getTableWriter();

        vals[0] = "Value A";
        vals[1] = 20;
        vals[2] = 40.0;
        vals[3] = 0;
        w2.append(cols, vals);

        vals[0] = "Value A";
        vals[1] = 30;
        vals[2] = 40.0;
        vals[3] = 1;
        w2.append(cols, vals);

        vals[0] = "Value A";
        vals[1] = 30;
        vals[2] = 50.0;
        vals[3] = 1;
        w2.append(cols, vals);

        vals[0] = "Value B";
        vals[1] = 30;
        vals[2] = 50.0;
        vals[3] = 1;
        w2.append(cols, vals);
    }

    @Test
    public void SchemaTest() // ComColumn. Manually add/remove tables/columns
    {
        DcTable t1 = schema.getSubTable("Table 1");
        DcTable t2 = schema.getSubTable("Table 2");

        // Finding by name and check various properties provided by the schema
        assertEquals(schema.getPrimitiveType("Decimal").getName(), "Decimal");

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
        DcColumn c15 = space.createColumn("Column 15", t1, schema.getPrimitiveType("Double"), false);
        c15.getData().setFormula("([Column 11]+10.0) * this.[Column 13]");

        // Evaluate column
        c15.getData().evaluate();

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
        DcColumn c15 = space.createColumn("Column 15", t1, schema.getPrimitiveType("String"), false);
        c15.getData().setFormula("call:java.lang.String.substring( [Column 12], 7, 8 )");

        // Evaluate column
        c15.getData().evaluate();

        assertEquals("0", c15.getData().getValue(0));
        assertEquals("1", c15.getData().getValue(1));
        assertEquals("2", c15.getData().getValue(2));

        //
        // Define a derived column with a definition
        //
        DcColumn c16 = space.createColumn("Column 15", t1, schema.getPrimitiveType("Double"), false);
        c16.getData().setFormula("call:java.lang.Math.pow( [Column 11] / 10.0, [Column 13] / 10.0 )");

        c16.getData().evaluate();

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

        DcColumn link = space.createColumn("Column Link", t2, t1, false);
        link.getData().setFormula("(( [Integer] [Column 11] = this.[Column 22], [Decimal] [Column 14] = 20.0 ))");

        // Evaluate column
        link.getData().evaluate();

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

        DcColumn c15 = space.createColumn("Agg of Column 23", t1, schema.getPrimitiveType("Double"), false);
        c15.getData().setFormula("AGGREGATE(facts=[Table 2], groups=[Table 1], measure=[Column 23], aggregator=SUM)");

        c15.getData().setValue(0.0);
        c15.getData().evaluate(); // {40, 140, 0}

        assertEquals(40.0, c15.getData().getValue(0));
        assertEquals(140.0, c15.getData().getValue(1));
        assertEquals(0.0, c15.getData().getValue(2)); // In fact, it has to be NaN or null (no values have been aggregated)

        //
        // Aggregation via a syntactic formula
        //
        DcColumn c16 = space.createColumn("Agg2 of Column 23", t1, schema.getPrimitiveType("Double"), false);
        c16.getData().setFormula("AGGREGATE(facts=[Table 2], groups=[Table 1], measure=[Column 23]*2.0 + 1, aggregator=SUM)");

        c16.getData().setValue(0.0);
        c16.getData().evaluate(); // {40, 140, 0}

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
        DcTable t3 = space.createTable("Table 3", schema.getRoot());

        DcColumn c31 = space.createColumn(t1.getName(), t3, t1, true); // {*20, 10, *30}
        DcColumn c32 = space.createColumn(t2.getName(), t3, t2, true); // {40, 40, *50, *50}

        t3.getData().populate();
        assertEquals(12, t3.getData().getLength());

        //
        // Add simple where expression
        //
        t3.getData().setWhereFormula("([Table 1].[Column 11] > 10) && this.[Table 2].[Column 23] == 50.0");

        t3.getData().populate();
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
        DcTable t3 = space.createTable("Table 3", t2);
        t3.getData().setWhereFormula("[Column 22] > 20.0 && this.Super.[Column 23] < 50");

        t3.getData().populate();
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
        DcTable t3 = space.createTable("Table 3", schema.getRoot());

        DcColumn c31 = space.createColumn("Column 31", t3, c21.getOutput(), true);

        // Create a generating column
        DcColumn c24 = space.createColumn(t3.getName(), t2, t3, false);

        c24.getData().setFormula("(( [String] [Column 31] = this.[Column 21] ))");
        c24.getData().setAppendData(true);

        t3.getData().populate();

        assertEquals(2, t3.getData().getLength());

        assertEquals(0, c24.getData().getValue(0));
        assertEquals(0, c24.getData().getValue(1));
        assertEquals(0, c24.getData().getValue(2));
        assertEquals(1, c24.getData().getValue(3));

        //
        // Defining a combination of "Column 21" and "Column 22" and project with 3 unique records in a new set
        //
        DcTable t4 = space.createTable("Table 4", schema.getRoot());

        DcColumn c41 = space.createColumn("Column 41", t4, c21.getOutput(), true);
        DcColumn c42 = space.createColumn("Column 42", t4, c22.getOutput(), true);

        // Create generating/import column
        DcColumn c25 = space.createColumn(t4.getName(), t2, t4, false);

        c25.getData().setFormula("(( [String] [Column 41] = this.[Column 21] , [Integer] [Column 42] = this.[Column 22] ))");
        c25.getData().setAppendData(true);

        t4.getData().populate();

        assertEquals(3, t4.getData().getLength());

        assertEquals(0, c25.getData().getValue(0));
        assertEquals(1, c25.getData().getValue(1));
        assertEquals(1, c25.getData().getValue(2));
        assertEquals(2, c25.getData().getValue(3));
    }

}
