package com.conceptoriented.com;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class SchemaTest {

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
        ComSchema schema = new SetTop("My Schema");

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
        vals[3] = 20.0;
        t1.getData().append(cols, vals);

        vals[0] = 10;
        vals[1] = "Record 1";
        vals[2] = 10.0;
        vals[3] = 20.0;
        t1.getData().append(cols, vals);

        vals[0] = 30;
        vals[1] = "Record 2";
        vals[2] = 30.0;
        vals[3] = 20.0;
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
        //
        // Prepare schema
        //
        ComSchema schema = createSampleSchema();

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
    public void ColumnDefinitionTest() // ComColumnDefinition. Defining new columns and evaluate them
    {
        //
        // Prepare schema and fill data
        //
        ComSchema schema = createSampleSchema();
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


}
