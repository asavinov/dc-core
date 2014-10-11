package com.conceptoriented.com;

import static org.junit.Assert.*;

import org.junit.Test;

public class SchemaTest {

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

	@Test
	public void SchemaCreationTest() {
		createSampleSchema();
	}

}
