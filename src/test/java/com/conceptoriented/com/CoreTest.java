package com.conceptoriented.com;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class CoreTest {

    protected ExprNode BuildExpr(String str)
    {
        ExprLexer lexer;
        ExprParser parser;
        ParseTree tree;
        String tree_str;
        ExprNode ast;

        ExprBuilder builder = new ExprBuilder();

        lexer = new ExprLexer(new ANTLRInputStream(str));
        parser = new ExprParser(new CommonTokenStream(lexer));
        tree = parser.expr();
        tree_str = tree.toStringTree(parser);

        ast = builder.visit(tree);

        return ast;
    }

    protected ComSchema createSampleSchema()
    {
        // Prepare schema
        ComSchema schema = new Schema("My Schema");

        // Table 1
        ComTable t1 = schema.createTable("Table 1");
        schema.addTable(t1, schema.getRoot(), null);

        ComColumn c11 = schema.createColumn("Column 11", t1, schema.getPrimitive("Integer"), true);
        c11.add();
        ComColumn c12 = schema.createColumn("Column 12", t1, schema.getPrimitive("String"), true);
        c12.add();
        ComColumn c13 = schema.createColumn("Column 13", t1, schema.getPrimitive("Double"), false);
        c13.add();
        ComColumn c14 = schema.createColumn("Column 14", t1, schema.getPrimitive("Decimal"), false);
        c14.add();

        // Table 2
        ComTable t2 = schema.createTable("Table 2");
        schema.addTable(t2, schema.getRoot(), null);

        ComColumn c21 = schema.createColumn("Column 21", t2, schema.getPrimitive("String"), true);
        c21.add();
        ComColumn c22 = schema.createColumn("Column 22", t2, schema.getPrimitive("Integer"), true);
        c22.add();
        ComColumn c23 = schema.createColumn("Column 23", t2, schema.getPrimitive("Double"), false);
        c23.add();
        ComColumn c24 = schema.createColumn("Table 1", t2, t1, false);
        c24.add();

        return schema;
    }

    protected void createSampleData(ComSchema schema)
    {
        //
        // Fill sample data in "Table 1"
        //
        ComTable t1 = schema.getSubTable("Table 1");

        ComColumn c11 = t1.getColumn("Column 11");
        ComColumn c12 = t1.getColumn("Column 12");
        ComColumn c13 = t1.getColumn("Column 13");
        ComColumn c14 = t1.getColumn("Column 14");

        ComColumn[] cols = new ComColumn[] { c11, c12, c13, c14 };
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
        ComTable t2 = schema.getSubTable("Table 2");

        ComColumn c21 = t2.getColumn("Column 21");
        ComColumn c22 = t2.getColumn("Column 22");
        ComColumn c23 = t2.getColumn("Column 23");
        ComColumn c24 = t2.getColumn("Table 1");

        cols = new ComColumn[] { c21, c22, c23, c24 };
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
	public void SchemaTest() { // ComColumn. Manually add/remove tables/columns
        Workspace workspace = new Workspace();

        //
        // Prepare schema
        //
        ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        ComTable t1 = schema.getSubTable("Table 1");
        ComTable t2 = schema.getSubTable("Table 2");

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
        Workspace workspace = new Workspace();

		//
        // Prepare schema and fill data
        //
        ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        createSampleData(schema);

        ComTable t1 = schema.getSubTable("Table 1");

        ComColumn c11 = t1.getColumn("Column 11");
        ComColumn c12 = t1.getColumn("Column 12");
        ComColumn c13 = t1.getColumn("Column 13");
        ComColumn c14 = t1.getColumn("Column 14");

        //
        // Define a derived column with a definition
        //
        ComColumn c15 = schema.createColumn("Column 15", t1, schema.getPrimitive("Double"), false);

        c15.getDefinition().setDefinitionType(ColumnDefinitionType.ARITHMETIC);
        ExprNode ast = BuildExpr("([Column 11]+10.0) * this.[Column 13]"); // ConceptScript source code: "[Decimal] [Column 15] <body of expression>"
        c15.getDefinition().setFormulaExpr(ast);
        
        c15.add();

        // Evaluate column
        c15.getDefinition().evaluate();

        assertEquals(600.0, c15.getData().getValue(0));
        assertEquals(200.0, c15.getData().getValue(1));
        assertEquals(1200.0, c15.getData().getValue(2));
    }

	@Test
    public void LinkTest()
    {
        Workspace workspace = new Workspace();
		
        //
        // Prepare schema and fill data
        //
        ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        createSampleData(schema);

        ComTable t1 = schema.getSubTable("Table 1");
        ComColumn c11 = t1.getColumn("Column 11"); // 20, 10, 30

        ComTable t2 = schema.getSubTable("Table 2");
        ComColumn c22 = t2.getColumn("Column 22"); // 20, 30, 30, 30

        //
        // Define a derived column with a definition
        //

        ComColumn link = schema.createColumn("Column Link", t2, t1, false);

        link.getDefinition().setDefinitionType(ColumnDefinitionType.LINK);
        ExprNode ast = BuildExpr("(( [Integer] [Column 11] = this.[Column 22], [Decimal] [Column 14] = 20.0 ))"); // Tuple structure corresponds to output table
        link.getDefinition().setFormulaExpr(ast);

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
        Workspace workspace = new Workspace();
		
        //
        // Prepare schema and fill data
        //
        ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        createSampleData(schema);

        ComTable t1 = schema.getSubTable("Table 1");

        ComTable t2 = schema.getSubTable("Table 2");

        ComColumn c23 = t2.getColumn("Column 23");
        ComColumn c24 = t2.getColumn("Table 1");

        //
        // Define aggregated column
        //
        ComColumn c15 = schema.createColumn("Agg of Column 23", t1, schema.getPrimitive("Double"), false);
        c15.getDefinition().setDefinitionType(ColumnDefinitionType.AGGREGATION);

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
        ComColumn c16 = schema.createColumn("Agg2 of Column 23", t1, schema.getPrimitive("Double"), false);
        c16.getDefinition().setDefinitionType(ColumnDefinitionType.AGGREGATION);

        ExprNode ast = BuildExpr("AGGREGATE(facts=[Table 2], groups=[Table 1], measure=[Column 23]*2.0 + 1, aggregator=SUM)");
        c16.getDefinition().setFormulaExpr(ast);

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
        Workspace workspace = new Workspace();

		ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        createSampleData(schema);

        ComTable t1 = schema.getSubTable("Table 1");
        ComTable t2 = schema.getSubTable("Table 2");

        //
        // Define a new product-set
        //
        ComTable t3 = schema.createTable("Table 3");
        t3.getDefinition().setDefinitionType(TableDefinitionType.PRODUCT);
        schema.addTable(t3, null, null);

        ComColumn c31 = schema.createColumn(t1.getName(), t3, t1, true); // {*20, 10, *30}
        c31.add();
        ComColumn c32 = schema.createColumn(t2.getName(), t3, t2, true); // {40, 40, *50, *50}
        c32.add();

        t3.getDefinition().populate();
        assertEquals(12, t3.getData().getLength());

        //
        // Add simple where expression
        //

        ExprNode ast = BuildExpr("([Table 1].[Column 11] > 10) && this.[Table 2].[Column 23] == 50.0");
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
        Workspace workspace = new Workspace();

		ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        createSampleData(schema);

        ComTable t2 = schema.getSubTable("Table 2");

        //
        // Define a new filter-set
        //
        ComTable t3 = schema.createTable("Table 3");

        ExprNode ast = BuildExpr("[Column 22] > 20.0 && this.Super.[Column 23] < 50");
        t3.getDefinition().setWhereExpr(ast);
        t3.getDefinition().setDefinitionType(TableDefinitionType.PRODUCT);

        schema.addTable(t3, t2, null);

        t3.getDefinition().populate();
        assertEquals(1, t3.getData().getLength());
        assertEquals(1, t3.getSuperColumn().getData().getValue(0));
    }

	@Test
    public void ProjectionTest() // Defining new tables via function projection and populate them
    {
        Workspace workspace = new Workspace();

        ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        createSampleData(schema);

        ComTable t2 = schema.getSubTable("Table 2");

        ComColumn c21 = t2.getColumn("Column 21");
        ComColumn c22 = t2.getColumn("Column 22");
        ComColumn c23 = t2.getColumn("Column 23");

        //
        // Project "Table 2" along "Column 21" and get 2 unique records in a new set "Value A" (3 references) and "Value B" (1 reference)
        //
        ComTable t3 = schema.createTable("Table 3");
        t3.getDefinition().setDefinitionType(TableDefinitionType.PROJECTION);
        schema.addTable(t3, null, null);

        ComColumn c31 = schema.createColumn("Column 31", t3, c21.getOutput(), true);
        c31.add();

        // Create a generating column
        ComColumn c24 = schema.createColumn(t3.getName(), t2, t3, false);

        ExprNode ast = BuildExpr("(( [String] [Column 31] = this.[Column 21] ))"); // Tuple structure corresponds to output table
        c24.getDefinition().setFormulaExpr(ast);
        c24.getDefinition().setDefinitionType(ColumnDefinitionType.LINK);
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
        ComTable t4 = schema.createTable("Table 4");
        t4.getDefinition().setDefinitionType(TableDefinitionType.PROJECTION);
        schema.addTable(t4, null, null);

        ComColumn c41 = schema.createColumn("Column 41", t4, c21.getOutput(), true);
        c41.add();
        ComColumn c42 = schema.createColumn("Column 42", t4, c22.getOutput(), true);
        c42.add();

        // Create generating/import column
        ComColumn c25 = schema.createColumn(t4.getName(), t2, t4, false);

        ExprNode ast2 = BuildExpr("(( [String] [Column 41] = this.[Column 21] , [Integer] [Column 42] = this.[Column 22] ))"); // Tuple structure corresponds to output table
        c25.getDefinition().setFormulaExpr(ast2);
        c25.getDefinition().setDefinitionType(ColumnDefinitionType.LINK);
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
        Workspace workspace = new Workspace();

		ComSchema schema = createSampleSchema();
        workspace.schemas.add(schema);
        schema.setWorkspace(workspace);

        ComTable integerType = schema.getPrimitive("Integer");
        ComTable doubleType = schema.getPrimitive("Double");

        ComTable detailsTable = ((Schema)schema).createFromCsv("C:\\Users\\savinov\\git\\conceptmix\\Test\\example2\\OrderDetails.csv", true);
        schema.addTable(detailsTable, null, null);
        ComTable productsTable = ((Schema)schema).createFromCsv("C:\\Users\\savinov\\git\\conceptmix\\Test\\example2\\Products.csv", true);
        schema.addTable(productsTable, null, null);
        ComTable categoriesTable = ((Schema)schema).createFromCsv("C:\\Users\\savinov\\git\\conceptmix\\Test\\example2\\Categories.csv", true);
        schema.addTable(categoriesTable, null, null);
        
        assertEquals(2155, detailsTable.getData().getLength());
        assertEquals(77, productsTable.getData().getLength());
        assertEquals(8, categoriesTable.getData().getLength());

        // Define a new arithmetic column: output is a computed primitive value
        ComColumn amountColumn = schema.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getDefinition().setFormulaExpr(BuildExpr("[UnitPrice] * [Quantity]"));
        amountColumn.getDefinition().setDefinitionType(ColumnDefinitionType.ARITHMETIC);
        amountColumn.add();
        amountColumn.getDefinition().evaluate();

        // Define two link column: output is a tuple
        ComColumn productColumn = schema.createColumn("Product", detailsTable, productsTable, false);
        productColumn.getDefinition().setFormulaExpr(BuildExpr("(( Integer [ProductID] = [ProductID] ))"));
        productColumn.getDefinition().setDefinitionType(ColumnDefinitionType.LINK);
        productColumn.add(); 
        productColumn.getDefinition().evaluate();
        
        ComColumn categoryColumn = schema.createColumn("Category", productsTable, categoriesTable, false);
        categoryColumn.getDefinition().setFormulaExpr(BuildExpr("(( Integer [CategoryID] = [CategoryID] ))"));
        categoryColumn.getDefinition().setDefinitionType(ColumnDefinitionType.LINK);
        categoryColumn.add();
        categoryColumn.getDefinition().evaluate();
    
        // Define a new aggregation column: output is an aggregation of a group of values
        ComColumn totalAmountColumn = schema.createColumn("Total Amount", categoriesTable, doubleType, false);
        totalAmountColumn.getDefinition().setFormulaExpr(BuildExpr("AGGREGATE(facts=[OrderDetails], groups=[Product].[Category], measure=[Amount], aggregator=SUM)"));
        totalAmountColumn.getDefinition().setDefinitionType(ColumnDefinitionType.AGGREGATION);
        totalAmountColumn.add();
        totalAmountColumn.getDefinition().evaluate();

        assertEquals(105268.6, totalAmountColumn.getData().getValue(6)); // cells = {286526.94999999995, 113694.75000000001, 177099.09999999995, 251330.5, 100726.8, 178188.80000000002, 105268.6, 141623.09000000003}

        ComColumn totalCountColumn = schema.createColumn("Total Count", categoriesTable, integerType, false);
        totalCountColumn.getDefinition().setFormulaExpr(BuildExpr("AGGREGATE(facts=[OrderDetails], groups=[Product].[Category], measure=[Amount], aggregator=COUNT)"));
        totalCountColumn.getDefinition().setDefinitionType(ColumnDefinitionType.AGGREGATION);
        totalCountColumn.add();
        totalCountColumn.getDefinition().evaluate();

        assertEquals(136, totalCountColumn.getData().getValue(6)); // cells = {404, 216, 334, 366, 196, 173, 136, 330}

    }

}
