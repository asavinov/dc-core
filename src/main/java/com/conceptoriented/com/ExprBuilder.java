package com.conceptoriented.com;

public class ExprBuilder extends ExprBaseVisitor<ExprNode> {

    static boolean accessAsThisNode = true; // Design alternative: access node can be represented either as a child or this node

	@Override
    public ExprNode visitExpr(ExprParser.ExprContext context) 
    {
        ExprNode n = new ExprNode();

        // Determine the type of expression

        if (context.op != null && context.op.getText().equals(".")) // Composition (dot) operation
        {
            n.setOperation(OperationType.CALL);
            n.setAction(ActionType.READ);

            ExprNode exprNode = visit(context.expr(0));
            if (exprNode != null)
            {
                n.addChild(exprNode);
            }

            ExprNode accessNode = visit(context.access());
            if (accessAsThisNode) // Represent accessor (after dot) by this node
            {
                if (context.access().name() != null) // Name of the function
                {
                    n.setName(accessNode.getName());
                }
                else // A definition of the function (lambda) is provided instead of name
                {
                    context.access().scope();
                    n.setName("lambda"); // Automatically generated name for a unnamed lambda
                }
            }
            else // Access node as a child (it can be named either by real method name or as a special 'method' with the method described represented elsewhere)
            {
                n.setName(".");
                if (accessNode != null)
                {
                    n.addChild(accessNode);
                }
            }
        }
        else if (context.op != null) // Arithmetic operations
        {
            n.setOperation(OperationType.CALL);
            String op = context.op.getText(); // Alternatively, context.GetChild(1).GetText()
            n.setName(op);

            if (op.equals("*")) n.setAction(ActionType.MUL);
            else if (op.equals("/")) n.setAction(ActionType.DIV);
            else if (op.equals("+")) n.setAction(ActionType.ADD);
            else if (op.equals("-")) n.setAction(ActionType.SUB);

            else if (op.equals("<=")) n.setAction(ActionType.LEQ);
            else if (op.equals(">=")) n.setAction(ActionType.GEQ);
            else if (op.equals(">")) n.setAction(ActionType.GRE);
            else if (op.equals("<")) n.setAction(ActionType.LES);

            else if (op.equals("==")) n.setAction(ActionType.EQ);
            else if (op.equals("!=")) n.setAction(ActionType.NEQ);

            else if (op.equals("&&")) n.setAction(ActionType.AND);
            else if (op.equals("||")) n.setAction(ActionType.OR);

            else ;

            ExprNode expr1 = visit(context.getChild(0));
            if (expr1 != null)
            {
                n.addChild(expr1);
            }

            ExprNode expr2 = visit(context.getChild(2));
            if (expr2 != null)
            {
                n.addChild(expr2);
            }
        }
        else if (context.expr() != null && context.getChild(0).getText().equals("(")) // Priority
        {
            n = visit(context.expr(0)); // Skip
        }
        else if (context.getChild(0).getText().equals("((") || context.getChild(0).getText().equals("TUPLE")) // Tuple
        {
            n.setOperation(OperationType.TUPLE);
            n.setAction(ActionType.READ); // Find

            // Find all members and store them in the tuple node
            int mmbrCount = context.member().size();
            for (int i = 0; i < mmbrCount; i++)
            {
                ExprNode mmbr = visit(context.member(i));
                if (mmbr != null)
                {
                    n.addChild(mmbr);
                }
            }
        }
        else if (context.literal() != null) // Literal
        {
            n.setOperation(OperationType.VALUE);
            n.setAction(ActionType.READ);

            String name = context.literal().getText();

            if (context.literal().INT() != null)
            {
            }
            else if (context.literal().DECIMAL() != null)
            {
            }
            else if (context.literal().STRING() != null)
            {
                name = name.substring(1, name.length() - 1); // Remove quotes
            }
            n.setName(name);
        }
        else if (context.getChild(0) instanceof ExprParser.AccessContext) // Access/call
        {
            n.setOperation(OperationType.CALL);
            n.setAction(ActionType.READ);

            ExprNode accessNode = visit(context.access());
            if (accessAsThisNode) // Represent accessor (after dot) by this node
            {
                if (context.access().name() != null) // Name of the function
                {
                    n.setName(accessNode.getName());
                }
                else // A definition of the function (lambda) is provided instead of name
                {
                    context.access().scope();
                    n.setName("lambda"); // Automatically generated name for a unnamed lambda
                }
            }
            else // Access node as a child (it can be named either by real method name or as a special 'method' with the method described represented elsewhere)
            {
                n.setName(".");
                if (accessNode != null)
                {
                    n.addChild(accessNode);
                }
            }

            // TODO: Read parameters and create nodes for them: context.access().param();
        }

        return n; 
    }

	@Override
    public ExprNode visitName(ExprParser.NameContext context) 
    {
        ExprNode n = new ExprNode();
        n.setOperation(OperationType.VALUE);
        n.setAction(ActionType.READ);

        if (context.DELIMITED_ID() != null)
        {
            String name = context.DELIMITED_ID().getText();
            n.setName(name.substring(1, name.length() - 1)); // Remove delimiters
        }
        else
        {
            n.setName(context.ID().getText());
        }

        return n;
    }

}