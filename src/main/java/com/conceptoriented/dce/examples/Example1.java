package com.conceptoriented.dce.examples;

import com.conceptoriented.dce.*;

public class Example1 {

    public static void main(String[] args) {
        String path = "C:\\Users\\savinov\\git\\dce-java\\src\\test\\resources\\example3";
        String detailsTableName = "Sales_SalesOrderDetail.txt";
        String productsTableName = "Production_Product.txt";
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
        DcTable detailsTable = ((Schema)schema).createFromCsv(path + "\\" + detailsTableName, true);
        schema.addTable(detailsTable, null, null);
        DcTable productsTable = ((Schema)schema).createFromCsv(path + "\\" + productsTableName, true);
        schema.addTable(productsTable, null, null);
        DcTable categoriesTable = ((Schema)schema).createFromCsv(path + "\\" + categoriesTableName, true);
        schema.addTable(categoriesTable, null, null);

        // Define a new arithmetic column: output is a computed primitive value
        DcColumn amountColumn = schema.createColumn("Amount", detailsTable, doubleType, false);
        amountColumn.getDefinition().setFormula("[UnitPrice] * [OrderQty]");
        amountColumn.add();
        amountColumn.getDefinition().evaluate();

        // 0. Turn off indexing during loading
        // 1. Make DC interfaces visible: either public or in a separate file
        // 2. Eliminate the need in ColumnDefinitionType. Set it automatically where parsing COEL
        // 3. Primitive types should be accessible simpler. Say,
        // 4. Introduce enumerator for identifying primitive types (instead of string)

    }

}
