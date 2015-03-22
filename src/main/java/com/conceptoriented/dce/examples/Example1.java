package com.conceptoriented.dce.examples;

import com.conceptoriented.dce.*;

public class Example1 {

    public static void main(String[] args) {
        String path = "src/test/resources/example3"; // Relative to project directory

        String detailsTableName = "Sales_SalesOrderDetail.txt";

        Workspace workspace = new Workspace();
        DcSchema schema = new Schema("Example 2");
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
